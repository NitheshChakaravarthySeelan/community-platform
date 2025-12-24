import "dotenv/config"; // Load environment variables as early as possible
import app, { cartService } from "./app"; // Use ES Module import and import cartService
import { initKafka, disconnectKafka } from "./kafka"; // Import Kafka functions
const port = process.env.PORT || 3000;
// Initialize Kafka and start the server
const startServer = async () => {
    try {
        await initKafka(cartService);
        app.listen(port, () => {
            console.log(`Server is running on http://localhost:${port}`);
        });
    }
    catch (error) {
        console.error("Failed to start server or Kafka:", error);
        process.exit(1);
    }
};
startServer();
// Handle graceful shutdown
process.on("SIGTERM", async () => {
    console.log("SIGTERM received. Shutting down gracefully...");
    await disconnectKafka();
    process.exit(0);
});
process.on("SIGINT", async () => {
    console.log("SIGINT received. Shutting down gracefully...");
    await disconnectKafka();
    process.exit(0);
});
//# sourceMappingURL=index.js.map