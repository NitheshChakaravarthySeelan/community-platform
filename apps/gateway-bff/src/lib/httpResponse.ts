import { NextResponse } from "next/server";

export const success = (data: unknown /** will be changed */, status = 200) => {
  return NextResponse.json(
    {
      success: true,
      data,
    },
    {
      status,
    },
  );
};

export const fail = (message: string, status = 400) => {
  return NextResponse.json(
    {
      success: false,
      message: message ?? "Something went wrong",
    },
    {
      status,
    },
  );
};

/**
 * Wrapping the fetch calls to backend services
 */
export const proxy = async (serviceUrl: string, options: RequestInit) => {
  try {
    const response = await fetch(serviceUrl, options);
    const contentType = response.headers.get("content-type");
    const isJson = contentType?.includes("application/json");

    const data = isJson ? await response.json() : await response.text();

    if (response.ok) {
      return success(data, response.status);
    } else {
      return fail(data.message, response.status);
    }
  } catch (error: Error | unknown) {
    const mesasge =
      error instanceof Error ? error.message : "Something went wrong";
    return fail(mesasge);
  }
};
