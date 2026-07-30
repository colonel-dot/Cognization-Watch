package com.example.cognitive.game.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cognitive.R
import com.example.cognitive.databinding.FragmentBrainTrainingBinding
import com.example.common.util.ItemSpacingDecoration
import com.example.cognitive.game.model.BrainTrainingRVModel
import com.example.cognitive.schulte.ui.SchulteGridActivity

class BrainTrainingFragment : Fragment() {

    private var _binding: FragmentBrainTrainingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrainTrainingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list: MutableList<BrainTrainingRVModel?> = ArrayList()
        list.add(BrainTrainingRVModel("舒尔特方格", R.drawable.cognitive_brain, "Completed", R.color.blue))
        list.add(BrainTrainingRVModel("数独", R.drawable.cognitive_brain, "敬请期待", R.color.green))
        list.add(BrainTrainingRVModel("中国象棋", R.drawable.cognitive_brain, "敬请期待", R.color.blue))
        list.add(BrainTrainingRVModel("形状识别", R.drawable.cognitive_brain, "敬请期待", R.color.orange))

        val adapter = BrainTrainingRVAdapter(list)
        binding.content.layoutManager = LinearLayoutManager(context)
        binding.content.adapter = adapter

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
        binding.content.addItemDecoration(itemSpacingDecoration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}