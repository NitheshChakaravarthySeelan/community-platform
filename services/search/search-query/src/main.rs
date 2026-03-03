use tonic::{transport::Server, Request, Response, Status};
use meilisearch_sdk::client::Client;
use std::env;
use std::sync::Arc;
use serde::{Deserialize, Serialize};

pub mod search_proto {
    tonic::include_proto!("search");
}

use search_proto::search_service_server::{SearchService, SearchServiceServer};
use search_proto::{SearchRequest, SearchResponse, ProductResult};

#[derive(Serialize, Deserialize, Debug, Clone)]
struct ProductDocument {
    id: String,
    name: String,
    description: String,
    price: f64,
    category: String,
    image_url: String,
}

pub struct MySearchService {
    meilisearch_client: Arc<Client>,
    index_name: String,
}

#[tonic::async_trait]
impl SearchService for MySearchService {
    async fn search_products(
        &self,
        request: Request<SearchRequest>,
    ) -> Result<Response<SearchResponse>, Status> {
        let req = request.into_inner();
        let index = self.meilisearch_client.index(&self.index_name);

        let search_result = index.search()
            .with_query(&req.query)
            .with_limit(if req.limit > 0 { req.limit as usize } else { 20 })
            .with_offset(req.offset as usize)
            .execute::<ProductDocument>()
            .await
            .map_err(|e| Status::internal(format!("Search failed: {}", e)))?;

        let products = search_result.hits.into_iter().map(|hit| ProductResult {
            id: hit.result.id,
            name: hit.result.name,
            description: hit.result.description,
            price: hit.result.price,
            category: hit.result.category,
            image_url: hit.result.image_url,
        }).collect();

        Ok(Response::new(SearchResponse { products }))
    }
}

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    tracing_subscriber::fmt::init();

    let meilisearch_url = env::var("MEILISEARCH_URL").expect("MEILISEARCH_URL must be set");
    let meilisearch_api_key = env::var("MEILISEARCH_API_KEY").expect("MEILISEARCH_API_KEY must be set");
    let meilisearch_index = env::var("MEILISEARCH_INDEX").expect("MEILISEARCH_INDEX must be set");
    let port = env::var("GRPC_SERVER_PORT").unwrap_or_else(|_| "50053".to_string());
    let addr = format!("0.0.0.0:{}", port).parse()?;

    let client = Arc::new(Client::new(&meilisearch_url, Some(&meilisearch_api_key))?);
    
    let search_service = MySearchService {
        meilisearch_client: client,
        index_name: meilisearch_index,
    };

    println!("Search Query Service (gRPC) listening on {}", addr);

    Server::builder()
        .add_service(SearchServiceServer::new(search_service))
        .serve(addr)
        .await?;

    Ok(())
}
