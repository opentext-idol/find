# Find API contract

The OpenAPI description of the Find web application's HTTP API. Builds a jar containing the YAML schema, split across
multiple files.

`find-api.yaml` is the specification itself; it references the other files.

After building the `core` module, you can perform additional checks on the specification by running this from the
`webapp` directory:

```
npx @redocly/cli lint core/target/openapi/find-api-combined.yaml
```

## Additional notes

- Some objects are bound rather than generated - see `apisToGenerate` and `modelsToGenerate` in `core/pom.xml` and `idol/pom.xml`.
- Bound objects in API requests are still validated according to the schema, even though they're not generated from it.
- Cross-file `$ref` works for schemas but not for parameters.
- Make sure schema names are unique across files or they'll get weird names when the files are combined.
- Required parameters are emitted first, regardless of the order they appear in the spec.
