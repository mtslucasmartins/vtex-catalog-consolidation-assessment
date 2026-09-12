#!/usr/bin/env python3
import re
import sqlite3
import sys
import unicodedata


def sql(value):
    if value is None:
        return "NULL"
    if isinstance(value, int):
        return str(value)
    return "'" + str(value).replace("'", "''") + "'"


def canonicalize(raw):
    if raw is None:
        return ""
    decomposed = unicodedata.normalize("NFD", str(raw))
    without_marks = "".join(char for char in decomposed if unicodedata.category(char) != "Mn")
    lowered = without_marks.lower()
    spaced = re.sub(r"\s+", "-", lowered)
    cleaned = re.sub(r"[^a-z0-9-]", "", spaced)
    collapsed = re.sub(r"-+", "-", cleaned)
    return collapsed.strip("-")


def sku(brand, name):
    return f"{canonicalize(brand)}#{canonicalize(name)}"


def emit(connection, source_table, source_columns, target_table, target_columns, text_columns=(), row_mapper=None):
    rows = list(connection.execute(f"SELECT {', '.join(source_columns)} FROM {source_table}"))
    if not rows:
        return
    values = []
    for row in rows:
        mapped = row_mapper(row) if row_mapper else row
        values.append("(" + ", ".join(
            sql(str(value) if column in text_columns and value is not None else value)
            for column, value in zip(target_columns, mapped)
        ) + ")")
    columns = ", ".join(target_columns)
    print(f"INSERT INTO {target_table} ({columns}) VALUES\n" + ",\n".join(values) + ";")


if len(sys.argv) != 2:
    raise SystemExit(f"usage: {sys.argv[0]} catalog.db")

with sqlite3.connect(sys.argv[1]) as database:
    emit(
        database,
        "Product",
        ("Id", "Name", "Brand", "Category"),
        "products",
        ("id", "sku", "name", "brand", "category"),
        ("sku",),
        lambda row: (row[0], sku(row[2], row[1]), row[1], row[2], row[3]),
    )
    emit(
        database,
        "SellerProduct",
        ("Id", "SellerName", "ProductId", "SellerProductId"),
        "products_sellers",
        ("id", "seller_name", "product_id", "seller_product_id"),
        ("seller_product_id",),
        lambda row: (row[0], row[1], row[2], row[3]),
    )

print("SELECT setval(pg_get_serial_sequence('products', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM products;")
print("SELECT setval(pg_get_serial_sequence('products_sellers', 'id'), COALESCE(MAX(id), 1), MAX(id) IS NOT NULL) FROM products_sellers;")
