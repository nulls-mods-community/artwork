# Artwork

A patcher library for masterpiece Null’s Brawl assets modifications.

### Project structure

* **core** contains all the logic and stuff
* **cli** is a module of standalone & executable JAR file (which was known as patch.jar)
* **android** is for wrapper over **core**, so it can be used in Android projects

### Core API

First, let’s look at the Mutator<T, V> interface. It represents an actor, which can work with data
of type T, and mutate (patch) the data with patches of type V. The idea is to have the same API for
different file types.

Currently, there is only one Mutator implementation available: TableMutator, which can work with
.CSV files and mutate (patch) them using the JSON objects.

A short example:

```
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
