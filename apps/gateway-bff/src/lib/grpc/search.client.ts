import * as grpc from "@grpc/grpc-js";
import { SearchServiceClient } from "../../proto/search";

const SEARCH_SERVICE_URL =
  process.env.SEARCH_SERVICE_GRPC_URL || "localhost:50053";

export const searchClient = new SearchServiceClient(
  SEARCH_SERVICE_URL,
  grpc.credentials.createInsecure(),
);
