package com.etherscan.app.extensions

import org.jooq.Record
import org.jooq.TableField

@Suppress("UNCHECKED_CAST")
fun <R : Record, T> TableField<R, T?>.notNull() = this as TableField<R, T>
