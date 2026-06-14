# Artwork

A patcher library for masterpiece Null’s Brawl assets modifications.

### Project structure

* **core** contains all the logic and stuff
* **cli** is a module of standalone & executable JAR file (which was known as patch.jar)
* **android** is for wrapper over **core**, so it can be used in Android projects

### Core API

The API is designed to be generic and support different asset types through a common interface.

First, let’s take a look at the Mutator<T, V> interface. It represents an actor, which can work with data
of type T, and mutate (patch) the data with patches of type V.

Currently, there is only Mutator implementation available is TableMutator, which can work with
.CSV files and mutate (patch) them using JSON objects.

A short example:

```java
Mutator<DataTable, JSONObject> mutator = TableMutator.getInstance();

DataTable table;
try (InputStream is = new FileInputStream("emotes.csv")) {
    table = mutator.load(is);
}

JSONObject patch = new JSONObject("{\"emoji_angry\": {\"LockedForChronos\": true} }");
mutator.apply(table, patch);

try (OutputStream os = new FileOutputStream("emotes.csv")) {
    mutator.save(table, os);
}
```

### CLI Usage

The CLI application is built on top of the Core API and currently supports patching CSV files only.

Usage:

```bash
java -jar cli.jar <content.json> <output directory> <input directories...>
```

Example:

```bash
java -jar cli.jar content.json patched/ assets1/ assets2/
```

Where:

* `content.json` contains patch definitions.
* `patched/` is the output directory and will be created automatically if it does not exist.
* `assets1/` and `assets2/` are directories containing `.csv` files to be patched.

#### Output behavior

* The output directory (`patched/` in the example) is created automatically if it does not exist.
* Existing files inside the output directory are not cleaned or removed.
  * This means stale or unrelated files may remain there if they already existed.
  * It is the user‘s responsibility to ensure the output directory is clean if needed.

* Only files that are actually affected by the patch are written into the output directory.
  * Files that are not modified by the patch are not copied into `patched/`.

* The directory structure of the input assets is preserved in the output.

Example:

```bash
> tree
.
├── assets1
│   └── csv_logic
│       └── emotes.csv
├── assets2
│   └── csv_logic
│       └── emotes.csv
└── content.json

5 directories, 3 files
```

will be patched as:

```
> tree patched
patched
└── csv_logic
    └── emotes.csv

2 directories, 1 file
```

If the same file exists in multiple input directories, the file from the last provided directory takes priority.  

In the example above, `assets2/csv_logic/emotes.csv` will be used as the base file, because `assets2/` was specified after `assets1/` in the CLI arguments.

#### content.json format notes

The `content.json` format is intentionally designed to be compatible with the .NullsBrawlAssets manifest format. Therefore, all top-level keys beginning with `@` are treated as metadata and ignored by the CLI.

<!-- The special `$schema` key is also ignored. It can be used by editors and IDEs to associate the file with a JSON Schema and provide validation, completion, and documentation hints. -->

The other keys in the patch file must correspond to a CSV file name (without the `.csv` extension). Each value uses the same patch format accepted by `TableMutator`.

For example:

```json
{
    "@title": "My awesome modification",
    "@author": "John Doe",

    "emotes": {
        "emoji_angry": {
            "LockedForChronos": true
        }
    }
}
```

In this example, `@title` and `@author` are treated as metadata and are ignored during patching.
