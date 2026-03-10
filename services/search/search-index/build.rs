fn main() -> Result<(), Box<dyn std::error::Error>> {
    let proto_file = "../../../shared/proto/catalog_events.proto";
    let proto_include = "../../../shared/proto/";

    println!("cargo:rerun-if-changed={}", proto_file);
    
    prost_build::compile_protos(
        &[proto_file],
        &[proto_include],
    )?;
    Ok(())
}
