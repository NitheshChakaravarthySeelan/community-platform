fn main() -> Result<(), Box<dyn std::error::Error>> {
    let proto_files = &[
        "../../../shared/proto/catalog_events.proto",
        "../../../shared/proto/inventory.proto",
        "../../../shared/proto/common.proto",
    ];
    let proto_include = "../../../shared/proto/";

    for file in proto_files {
        println!("cargo:rerun-if-changed={}", file);
    }
    
    let mut config = prost_build::Config::new();
    config.type_attribute(".", "#[derive(serde::Deserialize, serde::Serialize)]");
    
    config.compile_protos(
        proto_files,
        &[proto_include],
    )?;
    Ok(())
}
