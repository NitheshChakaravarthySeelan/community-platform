// package: catalog_events
// file: catalog_events.proto

import * as jspb from "google-protobuf";

export class ProductUpdatedEvent extends jspb.Message {
  getProductId(): string;
  setProductId(value: string): void;

  getName(): string;
  setName(value: string): void;

  getDescription(): string;
  setDescription(value: string): void;

  getPrice(): number;
  setPrice(value: number): void;

  getQuantity(): number;
  setQuantity(value: number): void;

  getSku(): string;
  setSku(value: string): void;

  getImageUrl(): string;
  setImageUrl(value: string): void;

  getCategory(): string;
  setCategory(value: string): void;

  getManufacturer(): string;
  setManufacturer(value: string): void;

  getStatus(): string;
  setStatus(value: string): void;

  getVersion(): number;
  setVersion(value: number): void;

  getUpdatedAt(): string;
  setUpdatedAt(value: string): void;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): ProductUpdatedEvent.AsObject;
  static toObject(
    includeInstance: boolean,
    msg: ProductUpdatedEvent,
  ): ProductUpdatedEvent.AsObject;
  static extensions: { [key: number]: jspb.ExtensionFieldInfo<jspb.Message> };
  static extensionsBinary: {
    [key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>;
  };
  static serializeBinaryToWriter(
    message: ProductUpdatedEvent,
    writer: jspb.BinaryWriter,
  ): void;
  static deserializeBinary(bytes: Uint8Array): ProductUpdatedEvent;
  static deserializeBinaryFromReader(
    message: ProductUpdatedEvent,
    reader: jspb.BinaryReader,
  ): ProductUpdatedEvent;
}

export namespace ProductUpdatedEvent {
  export type AsObject = {
    productId: string;
    name: string;
    description: string;
    price: number;
    quantity: number;
    sku: string;
    imageUrl: string;
    category: string;
    manufacturer: string;
    status: string;
    version: number;
    updatedAt: string;
  };
}

export class ProductCreatedEvent extends jspb.Message {
  getProductId(): string;
  setProductId(value: string): void;

  getName(): string;
  setName(value: string): void;

  getDescription(): string;
  setDescription(value: string): void;

  getPrice(): number;
  setPrice(value: number): void;

  getQuantity(): number;
  setQuantity(value: number): void;

  getSku(): string;
  setSku(value: string): void;

  getImageUrl(): string;
  setImageUrl(value: string): void;

  getCategory(): string;
  setCategory(value: string): void;

  getManufacturer(): string;
  setManufacturer(value: string): void;

  getStatus(): string;
  setStatus(value: string): void;

  getVersion(): number;
  setVersion(value: number): void;

  getCreatedAt(): string;
  setCreatedAt(value: string): void;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): ProductCreatedEvent.AsObject;
  static toObject(
    includeInstance: boolean,
    msg: ProductCreatedEvent,
  ): ProductCreatedEvent.AsObject;
  static extensions: { [key: number]: jspb.ExtensionFieldInfo<jspb.Message> };
  static extensionsBinary: {
    [key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>;
  };
  static serializeBinaryToWriter(
    message: ProductCreatedEvent,
    writer: jspb.BinaryWriter,
  ): void;
  static deserializeBinary(bytes: Uint8Array): ProductCreatedEvent;
  static deserializeBinaryFromReader(
    message: ProductCreatedEvent,
    reader: jspb.BinaryReader,
  ): ProductCreatedEvent;
}

export namespace ProductCreatedEvent {
  export type AsObject = {
    productId: string;
    name: string;
    description: string;
    price: number;
    quantity: number;
    sku: string;
    imageUrl: string;
    category: string;
    manufacturer: string;
    status: string;
    version: number;
    createdAt: string;
  };
}

export class ProductDeletedEvent extends jspb.Message {
  getProductId(): string;
  setProductId(value: string): void;

  getSku(): string;
  setSku(value: string): void;

  serializeBinary(): Uint8Array;
  toObject(includeInstance?: boolean): ProductDeletedEvent.AsObject;
  static toObject(
    includeInstance: boolean,
    msg: ProductDeletedEvent,
  ): ProductDeletedEvent.AsObject;
  static extensions: { [key: number]: jspb.ExtensionFieldInfo<jspb.Message> };
  static extensionsBinary: {
    [key: number]: jspb.ExtensionFieldBinaryInfo<jspb.Message>;
  };
  static serializeBinaryToWriter(
    message: ProductDeletedEvent,
    writer: jspb.BinaryWriter,
  ): void;
  static deserializeBinary(bytes: Uint8Array): ProductDeletedEvent;
  static deserializeBinaryFromReader(
    message: ProductDeletedEvent,
    reader: jspb.BinaryReader,
  ): ProductDeletedEvent;
}

export namespace ProductDeletedEvent {
  export type AsObject = {
    productId: string;
    sku: string;
  };
}
