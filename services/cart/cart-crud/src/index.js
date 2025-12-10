import "dotenv/config"; // Load environment variables as early as possible
import app from "./app"; // Use ES Module import
const port = process.env.PORT || 3000;
app.listen(port, () => {
  console.log(`Server is running on http://localhost:${port}`);
});
//# sourceMappingURL=index.js.map
