// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'database.dart';

// ignore_for_file: type=lint
class $BookDocumentsTable extends BookDocuments
    with TableInfo<$BookDocumentsTable, BookDocument> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $BookDocumentsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
    'id',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _documentMeta = const VerificationMeta(
    'document',
  );
  @override
  late final GeneratedColumn<String> document = GeneratedColumn<String>(
    'document',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [id, document];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'book_documents';
  @override
  VerificationContext validateIntegrity(
    Insertable<BookDocument> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('document')) {
      context.handle(
        _documentMeta,
        document.isAcceptableOrUnknown(data['document']!, _documentMeta),
      );
    } else if (isInserting) {
      context.missing(_documentMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  BookDocument map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return BookDocument(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}id'],
      )!,
      document: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}document'],
      )!,
    );
  }

  @override
  $BookDocumentsTable createAlias(String alias) {
    return $BookDocumentsTable(attachedDatabase, alias);
  }
}

class BookDocument extends DataClass implements Insertable<BookDocument> {
  final int id;
  final String document;
  const BookDocument({required this.id, required this.document});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['document'] = Variable<String>(document);
    return map;
  }

  BookDocumentsCompanion toCompanion(bool nullToAbsent) {
    return BookDocumentsCompanion(id: Value(id), document: Value(document));
  }

  factory BookDocument.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return BookDocument(
      id: serializer.fromJson<int>(json['id']),
      document: serializer.fromJson<String>(json['document']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'document': serializer.toJson<String>(document),
    };
  }

  BookDocument copyWith({int? id, String? document}) =>
      BookDocument(id: id ?? this.id, document: document ?? this.document);
  BookDocument copyWithCompanion(BookDocumentsCompanion data) {
    return BookDocument(
      id: data.id.present ? data.id.value : this.id,
      document: data.document.present ? data.document.value : this.document,
    );
  }

  @override
  String toString() {
    return (StringBuffer('BookDocument(')
          ..write('id: $id, ')
          ..write('document: $document')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(id, document);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is BookDocument &&
          other.id == this.id &&
          other.document == this.document);
}

class BookDocumentsCompanion extends UpdateCompanion<BookDocument> {
  final Value<int> id;
  final Value<String> document;
  const BookDocumentsCompanion({
    this.id = const Value.absent(),
    this.document = const Value.absent(),
  });
  BookDocumentsCompanion.insert({
    this.id = const Value.absent(),
    required String document,
  }) : document = Value(document);
  static Insertable<BookDocument> custom({
    Expression<int>? id,
    Expression<String>? document,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (document != null) 'document': document,
    });
  }

  BookDocumentsCompanion copyWith({Value<int>? id, Value<String>? document}) {
    return BookDocumentsCompanion(
      id: id ?? this.id,
      document: document ?? this.document,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (document.present) {
      map['document'] = Variable<String>(document.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('BookDocumentsCompanion(')
          ..write('id: $id, ')
          ..write('document: $document')
          ..write(')'))
        .toString();
  }
}

abstract class _$ZenithDatabase extends GeneratedDatabase {
  _$ZenithDatabase(QueryExecutor e) : super(e);
  $ZenithDatabaseManager get managers => $ZenithDatabaseManager(this);
  late final $BookDocumentsTable bookDocuments = $BookDocumentsTable(this);
  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [bookDocuments];
}

typedef $$BookDocumentsTableCreateCompanionBuilder =
    BookDocumentsCompanion Function({Value<int> id, required String document});
typedef $$BookDocumentsTableUpdateCompanionBuilder =
    BookDocumentsCompanion Function({Value<int> id, Value<String> document});

class $$BookDocumentsTableFilterComposer
    extends Composer<_$ZenithDatabase, $BookDocumentsTable> {
  $$BookDocumentsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get document => $composableBuilder(
    column: $table.document,
    builder: (column) => ColumnFilters(column),
  );
}

class $$BookDocumentsTableOrderingComposer
    extends Composer<_$ZenithDatabase, $BookDocumentsTable> {
  $$BookDocumentsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get document => $composableBuilder(
    column: $table.document,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$BookDocumentsTableAnnotationComposer
    extends Composer<_$ZenithDatabase, $BookDocumentsTable> {
  $$BookDocumentsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get document =>
      $composableBuilder(column: $table.document, builder: (column) => column);
}

class $$BookDocumentsTableTableManager
    extends
        RootTableManager<
          _$ZenithDatabase,
          $BookDocumentsTable,
          BookDocument,
          $$BookDocumentsTableFilterComposer,
          $$BookDocumentsTableOrderingComposer,
          $$BookDocumentsTableAnnotationComposer,
          $$BookDocumentsTableCreateCompanionBuilder,
          $$BookDocumentsTableUpdateCompanionBuilder,
          (
            BookDocument,
            BaseReferences<_$ZenithDatabase, $BookDocumentsTable, BookDocument>,
          ),
          BookDocument,
          PrefetchHooks Function()
        > {
  $$BookDocumentsTableTableManager(
    _$ZenithDatabase db,
    $BookDocumentsTable table,
  ) : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$BookDocumentsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$BookDocumentsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$BookDocumentsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                Value<String> document = const Value.absent(),
              }) => BookDocumentsCompanion(id: id, document: document),
          createCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                required String document,
              }) => BookDocumentsCompanion.insert(id: id, document: document),
          withReferenceMapper: (p0) => p0
              .map(
                (e) => (
                  e.readTable<$BookDocumentsTable, BookDocument>(table),
                  BaseReferences<
                    _$ZenithDatabase,
                    $BookDocumentsTable,
                    BookDocument
                  >(db, table, e),
                ),
              )
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$BookDocumentsTableProcessedTableManager =
    ProcessedTableManager<
      _$ZenithDatabase,
      $BookDocumentsTable,
      BookDocument,
      $$BookDocumentsTableFilterComposer,
      $$BookDocumentsTableOrderingComposer,
      $$BookDocumentsTableAnnotationComposer,
      $$BookDocumentsTableCreateCompanionBuilder,
      $$BookDocumentsTableUpdateCompanionBuilder,
      (
        BookDocument,
        BaseReferences<_$ZenithDatabase, $BookDocumentsTable, BookDocument>,
      ),
      BookDocument,
      PrefetchHooks Function()
    >;

class $ZenithDatabaseManager {
  final _$ZenithDatabase _db;
  $ZenithDatabaseManager(this._db);
  $$BookDocumentsTableTableManager get bookDocuments =>
      $$BookDocumentsTableTableManager(_db, _db.bookDocuments);
}
