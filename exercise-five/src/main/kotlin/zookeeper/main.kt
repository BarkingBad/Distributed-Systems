package zookeeper

import org.apache.zookeeper.AsyncCallback.StatCallback
import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooKeeper
import org.apache.zookeeper.data.Stat
import java.io.IOException
import kotlin.concurrent.thread

fun traverseAllNodes(zk: ZooKeeper, prefix: String, znode: String): List<String> = "$prefix/$znode".let {
    zk.exists(it, false)?.run { listOf(it) +
        zk.getChildren(it, null).flatMap { child ->
            traverseAllNodes(zk, it, child)
        }
    } ?: emptyList()
}

class ChildrenWatcher(val origin: String, val znode: String, val zk: ZooKeeper) : Watcher, StatCallback {

    var children = mutableListOf<String>()

    private fun updateChildren(zk: ZooKeeper, origin: String, znode: String, children: MutableList<String>, watcher: Watcher): MutableList<String> =
        zk.getChildren(znode, watcher).also {
            (it - children).singleOrNull()?.let { "$znode/$it" }?.run {
                zk.exists(this, true, ChildrenWatcher(origin, this, zk), null)
                println(traverseAllNodes(zk, "", origin.drop(1)).size - 1)
            }
        }

    override fun process(event: WatchedEvent) {
        if (event.path == znode) {
            zk.exists(znode, true, this, null)
        }
    }

    override fun processResult(rc: Int, path: String, ctx: Any?, stat: Stat?) {
        when (KeeperException.Code.get(rc)) {
            KeeperException.Code.OK -> children = updateChildren(zk, origin, znode, children, this)
            else -> zk.exists(znode, true, this, null)
        }
    }
}

class ProcessWatcher(hostPort: String, val znode: String, val exec: String) : Watcher, StatCallback {

    val zk = ZooKeeper(hostPort, 3000, this)
    var process: Process? = null

    override fun process(event: WatchedEvent) {
        if (event.path == znode) {
            zk.exists(znode, true, this, null)
        }
    }

    override fun processResult(rc: Int, path: String, ctx: Any?, stat: Stat?) {
        when (KeeperException.Code.get(rc)) {
            KeeperException.Code.OK ->  {
                if(process == null) {
                    println("Starting child")
                    try {
                        process = Runtime.getRuntime().exec(exec)
                        thread {
                            while(true) {
                                try {
                                    process?.inputStream?.copyTo(System.out)
                                    process?.errorStream?.copyTo(System.err)
                                } catch (e: Exception) {}
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            KeeperException.Code.NONODE -> {
                if(process?.isAlive == true) {
                    println("Stopping child")
                    process?.destroy()
                    try {
                        process!!.waitFor()
                        process = null
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
            KeeperException.Code.SESSIONEXPIRED, KeeperException.Code.NOAUTH -> {
                return
            }
            else -> {
                zk.exists(znode, true, this, null)
            }
        }
    }

    init {
        zk.exists(znode, true, this, null)
        zk.exists(znode, true, ChildrenWatcher(znode, znode, zk), null)
    }
}

fun main() {
    try {
        ProcessWatcher("127.0.0.1:2181", "/z", "exercise-five/printAndSleep.sh")
        thread {
            while(true) {
                readLine()
                println(traverseAllNodes(ZooKeeper("127.0.0.1:2181", 3000, null), "", "z"))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}