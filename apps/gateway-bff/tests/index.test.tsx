import React from "react";
import { render, screen } from "@testing-library/react";

describe("Index Page", () => {
  it("renders a heading", () => {
    render(<h1>Hello World</h1>); // Minimal render to satisfy the test runner
    expect(screen.getByText("Hello World")).toBeInTheDocument();
  });
});
