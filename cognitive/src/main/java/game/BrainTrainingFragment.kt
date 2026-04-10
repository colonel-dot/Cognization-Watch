package game

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cognitive.R
import schulte.ui.SchulteGridActivity
import com.example.common.util.ItemSpacingDecoration

class BrainTrainingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_brain_training, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list: MutableList<BrainTrainingRVModel?> = ArrayList()
        list.add(BrainTrainingRVModel("舒尔特方格", R.drawable.brain, "Completed", R.color.blue))
        list.add(BrainTrainingRVModel("数独", R.drawable.brain, "敬请期待", R.color.green))
        list.add(BrainTrainingRVModel("中国象棋", R.drawable.brain, "敬请期待", R.color.blue))
        list.add(BrainTrainingRVModel("形状识别", R.drawable.brain, "敬请期待", R.color.orange))

        val adapter = BrainTrainingRVAdapter(list)
        val recyclerView = view.findViewById<RecyclerView?>(R.id.content)
        recyclerView?.setLayoutManager(LinearLayoutManager(context))
        recyclerView?.setAdapter(adapter)

        adapter.setOnItemClickListener { position: Int ->
            val intent = when (position) {
                0 -> Intent(context, SchulteGridActivity::class.java)
                else -> null
            }

            if (intent != null) {
                startActivity(intent)
            }
        }

        val itemSpacingDecoration = ItemSpacingDecoration(context, 20, false)
        recyclerView?.addItemDecoration(itemSpacingDecoration)
    }
}