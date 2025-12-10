import type { Request, Response, NextFunction } from "express"; // Import types
import { validationResult } from "express-validator"; // Use ES module import

export const validateRequest = (
  req: Request,
  res: Response,
  next: NextFunction,
) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }
  next();
};
