package tk.qcsoft.angelos

import tk.qcsoft.angelos.util.YamlUtils
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
     * default config file name
     */
    private val DEFAULT_CONFIG_NAME = "kyrios.yml"

    /**
     * get DSL context
     *
     * @return DSLContext
     */
    val context: DSLContext?
        get() {

            return YamlUtils.getCustomConfigOrDefault(DEFAULT_CONFIG_NAME,"enable",false)
                    .takeIf { it is Boolean && it }?.let {
                return DSL.using(this.configuration)
            }
        }

    init {

        YamlUtils.getCustomConfigOrDefault(DEFAULT_CONFIG_NAME,"enable",false)
            .takeIf { it is Boolean && it }
            ?.let  {
                this.dataSource = HikariDataSource().apply {
                this.driverClassName = YamlUtils.getCustomConfig(DEFAULT_CONFIG_NAME,"driverName")
                this.jdbcUrl = YamlUtils.getCustomConfig(DEFAULT_CONFIG_NAME,"url")
                this.username = YamlUtils.getCustomConfig(DEFAULT_CONFIG_NAME,"username")
                this.password = YamlUtils.getCustomConfig(DEFAULT_CONFIG_NAME,"password")

                this.addDataSourceProperty("maxConnectionsPerPartition"
                        , YamlUtils.getCustomConfig(DEFAULT_CONFIG_NAME,"maxConnectionsPerPartition"))
                this.addDataSourceProperty("minConnectionsPerPartition"
                        , YamlUtils.getCustomConfig(DEFAULT_CONFIG_NAME,"minConnectionsPerPartition"))
                this.addDataSourceProperty("idleConnectionTestPeriodInMinutes"
                        , YamlUtils.getCustomConfig(DEFAULT_CONFIG_NAME,"idleConnectionTestPeriodInMinutes"))
                this.addDataSourceProperty("maxConnectionAgeInSeconds"
                        , YamlUtils.getCustomConfig(DEFAULT_CONFIG_NAME,"maxConnectionAgeInSeconds"))
                this.addDataSourceProperty("idleMaxAgeInMinutes"
                        , YamlUtils.getCustomConfig(DEFAULT_CONFIG_NAME,"idleMaxAgeInMinutes"))
                this.addDataSourceProperty("cachePrepStmts"
                        , YamlUtils.getCustomConfig(DEFAULT_CONFIG_NAME,"cachePrepStmts"))
                this.addDataSourceProperty("prepStmtCacheSize"
                        , YamlUtils.getCustomConfig(DEFAULT_CONFIG_NAME,"prepStmtCacheSize"))
                this.addDataSourceProperty("prepStmtCacheSqlLimit"
                        , YamlUtils.getCustomConfig(DEFAULT_CONFIG_NAME,"prepStmtCacheSqlLimit"))
                this.addDataSourceProperty("connectionTimeout"
                        , YamlUtils.getCustomConfig(DEFAULT_CONFIG_NAME,"connectionTimeout"))
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




