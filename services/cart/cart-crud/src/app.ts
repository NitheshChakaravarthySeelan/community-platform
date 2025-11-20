const express = require("express");
const { cartRoutes } = require("./routes/cart.routes");

const app = express();

app.use(express.json());

app.get("/", (req, res) => {
  res.send("Cart CRUD Service is running!");
});

app.use("/api/v1/carts", cartRoutes);

module.exports = app;
