import org.rspeer.runetek.adapter.component.Item
import org.rspeer.runetek.api.commons.StopWatch
import org.rspeer.runetek.api.component.Bank
import org.rspeer.runetek.api.component.ItemTables
import org.rspeer.runetek.api.component.tab.Inventory
import org.rspeer.runetek.event.listeners.ItemTableListener
import org.rspeer.runetek.event.types.ItemTableEvent
import org.rspeer.runetek.event.types.ItemTableEvent.ChangeType.ITEM_ADDED
import org.rspeer.runetek.event.types.ItemTableEvent.ChangeType.ITEM_REMOVED
import org.rspeer.script.Script
import org.rspeer.script.ScriptMeta
import org.rspeer.ui.Log

const val CHOCOLATE_BAR = 1973
const val CHOCOLATE_DUST = 1975
const val KNIFE: Int = 946

@ScriptMeta(developer = "rkr35", name = "ChocoDusty", desc = "Script to convert chocolate bars into chocolate dust.")
class Main : Script(), ItemTableListener {
    private var knife: Item? = null
    private var bars: Int = 0
    private var dusts: Int = 0

    override fun loop(): Int {
        when {
            hasKnife() -> when {
                hasBars() -> makeDust()
                hasDust() -> depositDust()
                else -> getBars()
            }

            else -> getKnife()
        }

        return 50
    }

    private fun hasKnife(): Boolean {
        if (knife == null) {
            knife = Inventory.getFirst(KNIFE)
            return knife != null
        } else {
            return true
        }
    }

    private fun getKnife() {
        if (Bank.isOpen()) {
            if (Bank.contains(KNIFE)) {
                Bank.withdraw(KNIFE, 1)
            } else {
                fatal("getKnife()", "Bank doesn't have a Knife ($KNIFE).")
            }
        } else {
            Bank.open()
        }
    }

    private fun hasBars() = bars > 0

    private fun getBars() {
        if (Bank.isOpen()) {
            if (Bank.contains(CHOCOLATE_BAR)) {
                Bank.withdrawAll(CHOCOLATE_BAR)
            } else {
                fatal("getBars()", "There are no Chocolate bars ($CHOCOLATE_BAR) in the bank.")
            }
        } else {
            Bank.open()
        }
    }

    private fun hasDust() = dusts > 0

    private fun depositDust() {
        if (Bank.isOpen()) {
            Bank.depositAll(CHOCOLATE_DUST)
        } else {
            Bank.open()
        }
    }

    private fun makeDust() {
        if (Bank.isClosed()) {
            knife?.interact("Use")
            Inventory.getItemAt(bars + dusts - 1).interact("Use")
        } else {
            Bank.close()
        }
    }

    private fun fatal(topic: String, msg: String) {
        Log.severe(topic, msg)
        isStopping = true
    }

    override fun notify(e: ItemTableEvent?) {
        if (e?.tableKey == ItemTables.INVENTORY) {
            when (Triple(e.changeType, e.oldId, e.id)) {
                Triple(ITEM_REMOVED, CHOCOLATE_DUST, -1) -> dusts -= 1

                Triple(ITEM_ADDED, CHOCOLATE_BAR, CHOCOLATE_BAR) -> bars += 1

                Triple(ITEM_ADDED, CHOCOLATE_BAR, CHOCOLATE_DUST) -> {
                    bars -= 1
                    dusts += 1
                }
            }
        }
    }
}