package com.community.catalog.productread.application.command;

// import lombok.AllArgsConstructor; // Remove this import
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// @AllArgsConstructor // Remove this annotation
public class GetAllProductsQuery {
    // This query object can be empty if no parameters are needed for getting all products
    // Or it can hold pagination/filtering parameters if those are added later
}
