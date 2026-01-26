package com.example.cognitive.main

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import com.example.cognitive.R
import mine.ui.MineRecordFragment
import read_assessment.ui.RecordActivity
import schedule.ui.ScheduleActivity
import schulte.ui.SchulteGameActivity
import sports.vm.StepViewModel
import kotlin.getValue
import androidx.fragment.app.viewModels


class HomeFragment : Fragment() {

    lateinit var mIntent: Intent
    lateinit var btn_game: View
    lateinit var btn_speak: View
    lateinit var btn_schedule: View
    lateinit var tvSteps: TextView
    private val stepsViewModel: StepViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_game = view.findViewById<Button>(R.id.game_layout)
        btn_speak = view.findViewById<Button>(R.id.speak_layout)
        btn_schedule = view.findViewById<Button>(R.id.schedule_layout)
        tvSteps = view.findViewById<TextView>(R.id.tv_steps)
        btn_speak.setOnClickListener {
            mIntent = Intent(requireContext(), RecordActivity::class.java)
            startActivity(mIntent) }
        btn_game.setOnClickListener {
            mIntent = Intent(requireContext(), SchulteGameActivity::class.java)
            startActivity(mIntent)
        }
        btn_schedule.setOnClickListener {
            mIntent = Intent(requireContext(), ScheduleActivity::class.java)
            startActivity(mIntent)
        }
        stepsViewModel.stepCount.observe(viewLifecycleOwner) {
            tvSteps.text = "今日步数 $it"
        }
    }

    override fun onResume() {
        super.onResume()
        stepsViewModel.refreshToday()
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}