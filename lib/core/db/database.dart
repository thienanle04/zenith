import 'package:drift/drift.dart';
import 'package:drift_flutter/drift_flutter.dart';

part 'database.g.dart';

class BookDocuments extends Table {
  IntColumn get id => integer()();

  TextColumn get document => text()();

  @override
  Set<Column> get primaryKey => {id};
}

/// The on-device ledger. The domain reaches it through [BookRepository].
@DriftDatabase(tables: [BookDocuments])
class ZenithDatabase extends _$ZenithDatabase {
  ZenithDatabase([QueryExecutor? executor]) : super(executor ?? driftDatabase(name: 'zenith'));

  @override
  int get schemaVersion => 1;
}
