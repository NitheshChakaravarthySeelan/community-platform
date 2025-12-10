import { validationResult } from "express-validator"; // Use ES module import
export const validateRequest = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }
  next();
};
//# sourceMappingURL=validator.js.map
