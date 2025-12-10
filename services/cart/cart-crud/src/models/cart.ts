export interface CartItem {
  id: number;
  productId: number;
  quantity: number;
}

export interface CartItemDetails extends CartItem {
  name: string | undefined;
  price: number | undefined;
  image: string | undefined;
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
