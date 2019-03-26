package cn.qcsoft.angelos

import cn.qcsoft.angelos.util.YmlUtils
import com.zaxxer.hikari.HikariDataSource
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import java.io.Closeable
import java.io.IOException

class Kyrios
/**
 * Constructor
 */
private constructor() : Closeable {

    /**
     * database configuration
     */
    private lateinit var configuration: Configuration
    /**
     * database source
     */
    private lateinit var dataSource: HikariDataSource

    /**
     * get DSL context
     *
     * @return DSLContext
     */
    val context: DSLContext?
        get() {
            return YmlUtils.kyrios.getOrDefault("enable",false).takeIf { it is Boolean && it }?.let {
                return DSL.using(this.configuration)
            }
        }

    init {

        YmlUtils.kyrios.getOrDefault("enable",false).takeIf { it is Boolean && it }
            ?.let  {
                this.dataSource = HikariDataSource().apply {
                this.driverClassName = YmlUtils.kyrios["driverName"]
                this.jdbcUrl = YmlUtils.kyrios["url"]
                this.username = YmlUtils.kyrios["username"]
                this.password = YmlUtils.kyrios["password"]

                this.addDataSourceProperty("maxConnectionsPerPartition"
                        , YmlUtils.kyrios["maxConnectionsPerPartition"])
                this.addDataSourceProperty("minConnectionsPerPartition"
                        , YmlUtils.kyrios["minConnectionsPerPartition"])
                this.addDataSourceProperty("idleConnectionTestPeriodInMinutes"
                        , YmlUtils.kyrios["idleConnectionTestPeriodInMinutes"])
                this.addDataSourceProperty("maxConnectionAgeInSeconds"
                        , YmlUtils.kyrios["maxConnectionAgeInSeconds"])
                this.addDataSourceProperty("idleMaxAgeInMinutes"
                        , YmlUtils.kyrios["idleMaxAgeInMinutes"])
                this.addDataSourceProperty("cachePrepStmts"
                        , YmlUtils.kyrios["cachePrepStmts"])
                this.addDataSourceProperty("prepStmtCacheSize"
                        , YmlUtils.kyrios["prepStmtCacheSize"])
                this.addDataSourceProperty("prepStmtCacheSqlLimit"
                        , YmlUtils.kyrios["prepStmtCacheSqlLimit"])
                this.addDataSourceProperty("connectionTimeout"
                        , YmlUtils.kyrios["connectionTimeout"])
                }.also {
                    this.configuration = DefaultConfiguration()
                            .set(it)
                            .set(SQLDialect.MYSQL)
                }
            }

        }

        /**
         * Closes this stream and releases any system resources associated
         * with it. If the stream is already closed then invoking this
         * method has no effect.
         *
         *
         *  As noted in [AutoCloseable.close], cases where the
         * close may fail require careful attention. It is strongly advised
         * to relinquish the underlying resources and to internally
         * *mark* the `Closeable` as closed, prior to throwing
         * the `IOException`.
         *
         * @throws IOException if an I/O error occurs
         */
        @Throws(IOException::class)
        override fun close() {
            this.dataSource.takeIf { it.isClosed }?.close()
        }

        companion object {

            /**
             * Singleton
             *
             * @return instance
             */
            val me = Kyrios()

            /**
             * Context ref
             *
             * @return context
             */
            val ctx = me.context
            /**
             * transaction
             *
             * @param runnable
             */
            fun transaction(runnable: Runnable) {
                ctx?.transaction{ _ -> runnable.run() }
            }
        }
    }




