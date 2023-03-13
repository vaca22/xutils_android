package com.vaca.xutils_android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.xutils.DbManager
import org.xutils.DbManager.DaoConfig
import org.xutils.ex.DbException
import org.xutils.x
import java.io.File
import java.sql.Date


class MainActivity : AppCompatActivity() {
    val daoConfig:DaoConfig by lazy{ DaoConfig()
        .setDbName("test.db") // 不设置dbDir时, 默认存储在app的私有目录.
        .setDbDir(getExternalFilesDir(null)!!) // "sdcard"的写法并非最佳实践, 这里为了简单, 先这样写了.
        .setDbVersion(2)
        .setDbOpenListener { db -> // 开启WAL, 对写入加速提升巨大
            db.database.enableWriteAheadLogging()
        }
        .setDbUpgradeListener { db, oldVersion, newVersion ->
            // TODO: ...
            // db.addColumn(...);
            // db.dropTable(...);
            // ...
            // or
            // db.dropDb();
        }}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        x.task().run(Runnable
        // 异步执行
        {
            var db = try {
                x.getDb(daoConfig)
            } catch (e: DbException) {
                e.printStackTrace()
                return@Runnable
            }
            var result = ""
            var parentList: MutableList<Parent?> = ArrayList()
            for (i in 0..999) {
                val parent = Parent()
                parent.isAdmin = true
                parent.date = Date(1234)
                parent.time = Date(55)
                parent.email = i.toString() + "_@qq.com"
                parentList.add(parent)
            }
            var start = System.currentTimeMillis()
            for (parent in parentList) {
                try {
                    db.save(parent)
                } catch (ex: DbException) {
                    ex.printStackTrace()
                }
            }
            result += """
                插入1000条数据:${System.currentTimeMillis() - start}ms
                
                """.trimIndent()
            start = System.currentTimeMillis()
            try {
                parentList =
                    db.selector(Parent::class.java).orderBy("id", true).limit(1000).findAll()
            } catch (ex: DbException) {
                ex.printStackTrace()
            }
            result += """
                查找1000条数据:${System.currentTimeMillis() - start}ms
                
                """.trimIndent()
            start = System.currentTimeMillis()
            try {
                db.delete(parentList)
            } catch (ex: DbException) {
                ex.printStackTrace()
            }
            result += """
                删除1000条数据:${System.currentTimeMillis() - start}ms
                
                """.trimIndent()

            // 批量插入
            parentList = ArrayList()
            for (i in 0..999) {
                val parent = Parent()
                parent.isAdmin = true
                parent.date = Date(1234)
                parent.time = Date(66)
                parent.email = i.toString() + "_@qq.com"
                parentList.add(parent)
            }
            start = System.currentTimeMillis()
            try {
                db.save(parentList)
            } catch (ex: DbException) {
                ex.printStackTrace()
            }
            result += """
                批量插入1000条数据:${System.currentTimeMillis() - start}ms
                
                """.trimIndent()
            try {
                parentList =
                    db.selector(Parent::class.java).orderBy("id", true).limit(1000).findAll()
                db.delete(parentList)
            } catch (ex: DbException) {
                ex.printStackTrace()
            }
            val finalResult = result
            x.task().post { Log.e("setText",finalResult) }
        })
    }
}