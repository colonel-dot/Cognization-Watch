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
import com.example.cognitive.main.ConMainActivity
import schulte.ui.SchulteGridActivity
import util.ItemSpacingDecoration
import util.OnItemClickListener


class BrainTrainingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_brain_training, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list: MutableList<BrainTrainingRVModel?> = ArrayList<BrainTrainingRVModel?>()
        list.add(BrainTrainingRVModel("舒尔特方格", R.drawable.brain, "Completed", R.color.blue))
        list.add(BrainTrainingRVModel("数独", R.drawable.brain, "敬请期待", R.color.green))
        list.add(BrainTrainingRVModel("中国象棋", R.drawable.brain, "敬请期待", R.color.blue))
        list.add(BrainTrainingRVModel("形状识别", R.drawable.brain, "敬请期待", R.color.orange))

        val adapter = BrainTrainingRVAdapter(list)
        val recyclerView = view.findViewById<RecyclerView?>(R.id.content)
        recyclerView.setLayoutManager(LinearLayoutManager(getContext()))
        recyclerView.setAdapter(adapter)

        adapter.setOnItemClickListener(OnItemClickListener { position: Int ->
            var intent: Intent? = null
            intent = when (position) {
                0 -> Intent(getContext(), SchulteGridActivity::class.java)
                else -> null
            }

            if (intent != null) {
                startActivity(intent)
            }
        })

        val itemSpacingDecoration = ItemSpacingDecoration(getContext(), 20, false)
        recyclerView.addItemDecoration(itemSpacingDecoration)
    }

    companion object {
        private const val ARG_PARAM1 = "param1"

        private const val ARG_PARAM2 = "param2"

        fun newInstance(param1: String?, param2: String?): BrainTrainingFragment {
            val fragment = BrainTrainingFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.setArguments(args)
            return fragment
        }
    }
}