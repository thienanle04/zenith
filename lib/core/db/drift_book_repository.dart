import 'package:drift/drift.dart';

import 'package:zenith/core/db/database.dart';
import 'package:zenith/domain/book/book_codec.dart';
import 'package:zenith/domain/book/book_repository.dart';
import 'package:zenith/domain/book/snapshot.dart';

/// Stores the book as one JSON document in Drift.
class DriftBookRepository implements BookRepository {
  DriftBookRepository(this._database);

  final ZenithDatabase _database;

  @override
  Future<BookSnapshot> load() async {
    final row = await (_database.select(_database.bookDocuments)..where((table) => table.id.equals(1))).getSingleOrNull();
    if (row == null) {
      return BookSnapshot.empty;
    }
    return BookCodec.decode(row.document);
  }

  @override
  Future<void> save(BookSnapshot book) {
    return _database.into(_database.bookDocuments).insert(
      BookDocumentsCompanion(id: const Value(1), document: Value(BookCodec.encode(book))),
      mode: InsertMode.insertOrReplace,
    );
  }
}
