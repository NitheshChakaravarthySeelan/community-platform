fn main() -> Result<(), Box<dyn std::error::Error>> {
    tonic_build::configure()
        .build_server(true)
        .compile(
            &["/home/nithesh/WindowsDrive/Coding/Project/Coding/Project/Community/community-platform/shared/proto/product_lookup.proto"],
            &["/home/nithesh/WindowsDrive/Coding/Project/Coding/Project/Community/community-platform/shared/proto"],
        )?;
    Ok(())
}