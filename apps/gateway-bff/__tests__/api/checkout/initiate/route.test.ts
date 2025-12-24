import request from 'supertest';
import { NextRequest, NextResponse } from 'next/server';
import { POST } from '../../../src/app/api/checkout/initiate/route'; // Adjust path as necessary
import { Kafka, Producer } from 'kafkajs';

// Mock Kafka producer
jest.mock('kafkajs', () => {
  const mockProducer = {
    connect: jest.fn(),
    disconnect: jest.fn(),
    send: jest.fn(),
  };
  return {
    Kafka: jest.fn(() => ({
      producer: jest.fn(() => mockProducer),
    })),
    // Need to export Partitioners if it's used somewhere in actual code
    // Partitioners: {
    //   LegacyPartitioner: jest.fn(),
    // },
  };
});

const mockKafkaProducer = new Kafka({
  clientId: 'test-producer',
  brokers: ['localhost:29092'],
}).producer();


describe('API Route: POST /api/checkout/initiate', () => {
  beforeEach(() => {
    // Clear all mocks before each test
    jest.clearAllMocks();
    // Ensure producer.connect is resolved
    (mockKafkaProducer.connect as jest.Mock).mockResolvedValue(undefined);
    // Ensure producer.send is resolved
    (mockKafkaProducer.send as jest.Mock).mockResolvedValue(undefined);
  });

  it('should return 202 and send CheckoutInitiatedEvent on valid input', async () => {
    const mockBody = {
      userId: 'test-user-id',
      items: [{ productId: 'prod-1', quantity: 1 }],
      totalAmount: 50.00,
    };

    // Create a mock NextRequest
    const mockRequest = {
      json: async () => mockBody,
    } as NextRequest;

    const response = await POST(mockRequest);
    const jsonResponse = await response.json();

    expect(response.status).toBe(202);
    expect(jsonResponse).toHaveProperty('message', 'Checkout initiated successfully');
    expect(jsonResponse).toHaveProperty('orderId');
    expect(jsonResponse).toHaveProperty('status', 'PROCESSING');
    expect(mockKafkaProducer.send).toHaveBeenCalledTimes(1);

    const sentMessage = (mockKafkaProducer.send as jest.Mock).mock.calls[0][0].messages[0];
    const parsedValue = JSON.parse(sentMessage.value);

    expect(parsedValue.type).toBe('CheckoutInitiatedEvent');
    expect(parsedValue.orderId).toBe(jsonResponse.orderId);
    expect(parsedValue.userId).toBe(mockBody.userId);
    expect(parsedValue.items).toEqual(mockBody.items);
    expect(parsedValue.totalAmount).toBe(mockBody.totalAmount);
    expect(parsedValue).toHaveProperty('timestamp');
  });

  it('should return 400 on missing userId', async () => {
    const mockBody = {
      items: [{ productId: 'prod-1', quantity: 1 }],
      totalAmount: 50.00,
    };
    const mockRequest = { json: async () => mockBody } as NextRequest;

    const response = await POST(mockRequest);
    const jsonResponse = await response.json();

    expect(response.status).toBe(400);
    expect(jsonResponse).toHaveProperty('error', 'Missing required fields: userId, items, totalAmount');
    expect(mockKafkaProducer.send).not.toHaveBeenCalled();
  });

  it('should return 400 on missing items', async () => {
    const mockBody = {
      userId: 'test-user-id',
      totalAmount: 50.00,
    };
    const mockRequest = { json: async () => mockBody } as NextRequest;

    const response = await POST(mockRequest);
    const jsonResponse = await response.json();

    expect(response.status).toBe(400);
    expect(jsonResponse).toHaveProperty('error', 'Missing required fields: userId, items, totalAmount');
    expect(mockKafkaProducer.send).not.toHaveBeenCalled();
  });

  it('should return 400 on missing totalAmount', async () => {
    const mockBody = {
      userId: 'test-user-id',
      items: [{ productId: 'prod-1', quantity: 1 }],
    };
    const mockRequest = { json: async () => mockBody } as NextRequest;

    const response = await POST(mockRequest);
    const jsonResponse = await response.json();

    expect(response.status).toBe(400);
    expect(jsonResponse).toHaveProperty('error', 'Missing required fields: userId, items, totalAmount');
    expect(mockKafkaProducer.send).not.toHaveBeenCalled();
  });

  it('should return 500 on Kafka producer error', async () => {
    const mockBody = {
      userId: 'test-user-id',
      items: [{ productId: 'prod-1', quantity: 1 }],
      totalAmount: 50.00,
    };
    const mockRequest = { json: async () => mockBody } as NextRequest;

    // Simulate Kafka producer error
    (mockKafkaProducer.send as jest.Mock).mockRejectedValue(new Error('Kafka connection failed'));

    const response = await POST(mockRequest);
    const jsonResponse = await response.json();

    expect(response.status).toBe(500);
    expect(jsonResponse).toHaveProperty('error', 'Failed to initiate checkout');
    expect(mockKafkaProducer.send).toHaveBeenCalledTimes(1);
  });
});
