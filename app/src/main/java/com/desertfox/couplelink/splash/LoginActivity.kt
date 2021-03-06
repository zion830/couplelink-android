package com.desertfox.couplelink.splash

import android.content.Intent
import android.os.Bundle
import androidx.core.content.edit
import androidx.core.view.isVisible
import com.desertfox.couplelink.BaseActivity
import com.desertfox.couplelink.R
import com.desertfox.couplelink.chatting.MainActivity
import com.desertfox.couplelink.data.UserData
import com.desertfox.couplelink.model.request.LoginRequest
import com.desertfox.couplelink.model.responses.LoginModel
import com.desertfox.couplelink.model.responses.MemberModel
import com.desertfox.couplelink.util.*
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.util.exception.KakaoException
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseActivity() {

    private var callback: SessionCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        callback = SessionCallback()
        Session.getCurrentSession().addCallback(callback)
        if (!Session.getCurrentSession().checkAndImplicitOpen()) {
            login_btn.isVisible = true
            login_loading.isVisible = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        Session.getCurrentSession().removeCallback(callback)
    }

    private inner class SessionCallback : ISessionCallback {

        override fun onSessionOpened() {
            val loginSingle = coupleLinkApi.login(LoginRequest(Session.getCurrentSession().tokenInfo.accessToken))
            val meSingle = coupleLinkApi.getMe()
            Single.concat(loginSingle, meSingle).observeOn(AndroidSchedulers.mainThread()).subscribe({
                when (it) {
                    is LoginModel -> sharedPreferences().edit {
                        putString(ACCESS_TOKEN, it.accessToken)
                    }
                    is MemberModel -> {
                        UserData.currentMember = it
                        if (it.status == "COUPLE") {
                            coupleLinkApi.getCouple(it.coupleId)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ couple ->
                                    UserData.currentCouple = couple
                                    if (UserData.myMemberModel.name.isNullOrEmpty()) {
                                        startActivity(Intent(this@LoginActivity, InfoinputActivity::class.java))
                                    } else {
                                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                    }
                                    finish()
                                }, { e ->
                                    e.printStackTrace()
                                    toast(e.message.toString())
                                })
                        } else {
                            startActivity(Intent(this@LoginActivity, ConnectionActivity::class.java))
                            finish()
                        }
                    }
                }
            }, {
                it.printStackTrace()
                toast(it.message.toString())
            }).bind()
        }

        override fun onSessionOpenFailed(exception: KakaoException?) {
            if (exception != null) {
                e(exception.toString())
            }
        }
    }
}