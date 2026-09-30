## 2024-05-15 - Jetpack Compose Performance Optimizations
**Learning:** When using Jetpack Compose, `SimpleDateFormat` instances created inside `LazyColumn` or standard loops are recreated on every recomposition. Also, `LazyColumn` without unique keys causes unnecessary recompositions of list items.
**Action:** Use `remember { SimpleDateFormat(...) }` to hoist formatting objects. Always use `key = { it.id }` inside `items` for `LazyColumn` to ensure Compose tracks items efficiently during list updates.
