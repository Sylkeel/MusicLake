package com.cyl.music_hnust.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cyl.music_hnust.R;
import com.cyl.music_hnust.callback.UserCallback;
import com.cyl.music_hnust.model.User;
import com.cyl.music_hnust.model.UserInfo;
import com.cyl.music_hnust.model.UserStatus;
import com.cyl.music_hnust.utils.Constants;
import com.cyl.music_hnust.utils.StatusBarCompat;
import com.cyl.music_hnust.utils.SystemUtils;
import com.cyl.music_hnust.utils.ToastUtils;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.zhy.http.okhttp.OkHttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 作者：yonglong on 2016/8/11 18:17
 * 邮箱：643872807@qq.com
 * 版本：2.5
 */
public class LoginActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.cv)
    CardView cv;
    @Bind(R.id.main)
    RelativeLayout main;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.qqlogin)
    com.getbase.floatingactionbutton.FloatingActionButton qqlogin;
    @Bind(R.id.register)
    Button register;

    @Bind(R.id.usernameWrapper)
    TextInputLayout usernameWrapper;
    @Bind(R.id.passwordWrapper)
    TextInputLayout passwordWrapper;

    @Bind(R.id.progressBar)
    ProgressBar progressBar;
    //QQ第三方登录
    String APP_ID = "1104846425";
    Tencent mTencent;
    IUiListener loginListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //初始化黄油刀控件绑定框架
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usernameWrapper.setHint("用户名");
        passwordWrapper.setHint("密码");

    }

    @Override
    protected void listener() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.fab)
    public void loginto() {
        final String username = usernameWrapper.getEditText().getText().toString();
        final String password = passwordWrapper.getEditText().getText().toString();
        // TODO: 检查　
        if (!validatePassword(username)) {
            passwordWrapper.setError("邮箱或者学号");
        } else if (!validatePassword(password)) {
            passwordWrapper.setError("密码需为6~18位的数字和字母");
        } else {
            usernameWrapper.setErrorEnabled(false);
            passwordWrapper.setErrorEnabled(false);
            //TODO:登录
            progressBar.setVisibility(View.VISIBLE);
            fab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                @Override
                public void onHidden(FloatingActionButton fab) {
                    super.onHidden(fab);
                    login(username, password);
                }
            });
        }
    }

    //
    @OnClick(R.id.register)
    public void tofab() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.qqlogin)
    public void tologin() {
        progressBar.setVisibility(View.VISIBLE);
        qqLogin();
    }/**
     * 沉浸式状态栏
     */
    private void initSystemBar() {
        if (SystemUtils.isKITKAT()) {
            int top = StatusBarCompat.getStatusBarHeight(this);
            main.setPadding(0, top, 0, 0);
        }
    }

    private void login(String username, String password) {

        OkHttpUtils.post()//
                .url(Constants.LOGIN_URL)//
                .addParams(Constants.USER_EMAIL, username)//
                .addParams(Constants.PASSWORD, password)//
                .build()//
                .execute(new UserCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        progressBar.setVisibility(View.GONE);
                        fab.show();
                        ToastUtils.show(LoginActivity.this, "网路异常，请稍后！");
                    }

                    @Override
                    public void onResponse(UserInfo response) {
                        Log.e("===", response.toString());
                        progressBar.setVisibility(View.GONE);
                        fab.show();
                        if (response.getStatus() == 1) {
                            //保存用户信息
                            UserStatus.savaUserInfo(getApplicationContext(), response.getUser());
                            UserStatus.saveuserstatus(getApplicationContext(), true);
                            ToastUtils.show(LoginActivity.this, response.getMessage());
                            finish();
                        } else {
                            ToastUtils.show(LoginActivity.this, response.getMessage());
                        }
                    }
                });
    }

    //判断密码是否合法
    public boolean validatePassword(String password) {
        return password.length() > 5 && password.length() <= 18;
    }

    /**
     * 实现QQ第三方登录
     */
    public void qqLogin() {
        //QQ第三方登录
        mTencent = Tencent.createInstance(APP_ID, this.getApplicationContext());
        mTencent.login(this, "all", loginListener);
        loginListener = new IUiListener() {
            @Override
            public void onComplete(Object o) {
                progressBar.setVisibility(View.GONE);
                //登录成功后回调该方法,可以跳转相关的页面
                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                JSONObject object = (JSONObject) o;
                try {
                    String accessToken = object.getString("access_token");
                    String expires = object.getString("expires_in");
                    String openID = object.getString("openid");
                    mTencent.setAccessToken(accessToken, expires);
                    mTencent.setOpenId(openID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(UiError uiError) {

            }

            @Override
            public void onCancel() {

            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == com.tencent.connect.common.Constants.REQUEST_LOGIN) {
            if (resultCode == -1) {
                Tencent.onActivityResultData(requestCode, resultCode, data, loginListener);
                Tencent.handleResultData(data, loginListener);
                com.tencent.connect.UserInfo info = new com.tencent.connect.UserInfo(this, mTencent.getQQToken());
                info.getUserInfo(new IUiListener() {
                    @Override
                    public void onComplete(Object o) {
                        try {
                            JSONObject info = (JSONObject) o;
                            String nickName = info.getString("nickname");//获取用户昵称
                            String iconUrl = info.getString("figureurl_qq_2");//获取用户头像的url
                            String gender = info.getString("gender");//获取用户性别
                            User user=new User();
                            user.setUser_name(nickName);
                            user.setUser_img(iconUrl);
                            user.setUser_sex(gender);

                            UserStatus.savaUserInfo(getApplicationContext(),user);
                            UserStatus.saveuserstatus(getApplicationContext(),true);
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(UiError uiError) {

                    }

                    @Override
                    public void onCancel() {

                    }
                });
            }
        }
    }


}