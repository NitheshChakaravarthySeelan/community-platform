import * as grpc from "@grpc/grpc-js";
import { AgentServiceClient, AgentServiceService } from "@proto/agent";
import { makeGenericClientConstructor } from "@grpc/grpc-js";

export class AgentGrpcClient {
  private client: AgentServiceClient;

  constructor(address: string) {
    const ClientConstructor = makeGenericClientConstructor(
      AgentServiceService,
      "agent.v1.AgentService",
    );
    this.client = new ClientConstructor(
      address,
      grpc.credentials.createInsecure(),
    ) as unknown as AgentServiceClient;
  }

  executeWorkflow(userQuery: string, userId: string) {
    return this.client.executeWorkflow({ userQuery, userId });
  }
}

export const agentClient = new AgentGrpcClient(
  process.env.AGENT_SERVICE_ADDR || "localhost:50050",
);
