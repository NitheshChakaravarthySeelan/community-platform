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
  console.log(`Proxying request to: ${serviceUrl}`);
  try {
    const response = await fetch(serviceUrl, options);
    const contentType = response.headers.get("content-type");
    const isJson = contentType?.includes("application/json");

    const data = isJson ? await response.json() : await response.text();

    if (response.ok) {
      return success(data, response.status);
    } else {
      console.error(
        `Proxy request failed with status ${response.status}:`,
        data,
      );
      const errorMessage =
        isJson && data.message
          ? data.message
          : typeof data === "string"
            ? data
            : JSON.stringify(data);
      return fail(errorMessage, response.status);
    }
  } catch (error: Error | unknown) {
    const message =
      error instanceof Error ? error.message : "Something went wrong";
    console.error(`Proxy request exception: ${message}`);
    return fail(message);
  }
};
