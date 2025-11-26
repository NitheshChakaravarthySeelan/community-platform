export interface CartItem {
  id: number;
  productId: number;
  quantity: number;
}

export interface CartItemDetails extends CartItem {
  name: string;
  price: number;
  image: string;
}

export interface Cart {
  id: number;
  userId: number;
  items: CartItem[];
  createdAt: Date;
  updatedAt: Date;
}

export interface CartDetails extends Cart {
  items: CartItemDetails[];
  totalPrice: number;
}
