package com.netease.yunxin.app.im.main.mine.setting;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * OpenClaw配置信息ViewModel
 * 处理配置页面的业务逻辑和数据管理
 */
public class ConfigInfoViewModel extends AndroidViewModel {

    // 配置数据
    private final MutableLiveData<String> appKey = new MutableLiveData<>("");
    private final MutableLiveData<String> account = new MutableLiveData<>("");
    private final MutableLiveData<String> token = new MutableLiveData<>("");
    private final MutableLiveData<String> openClawAccount = new MutableLiveData<>("");
    
    // UI状态
    private final MutableLiveData<Boolean> saveButtonEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<SaveResult> saveResult = new MutableLiveData<>();
    private final MutableLiveData<String> validationError = new MutableLiveData<>();

    public ConfigInfoViewModel(@NonNull Application application) {
        super(application);
        updateSaveButtonState();
    }

    // Getter方法
    public LiveData<String> getAppKey() {
        return appKey;
    }

    public LiveData<String> getAccount() {
        return account;
    }

    public LiveData<String> getToken() {
        return token;
    }

    public LiveData<String> getOpenClawAccount() {
        return openClawAccount;
    }

    public LiveData<Boolean> getSaveButtonEnabled() {
        return saveButtonEnabled;
    }

    public LiveData<SaveResult> getSaveResult() {
        return saveResult;
    }

    public LiveData<String> getValidationError() {
        return validationError;
    }

    // Setter方法
    public void setAppKey(String value) {
        appKey.setValue(value != null ? value : "");
        updateSaveButtonState();
    }

    public void setAccount(String value) {
        account.setValue(value != null ? value : "");
        updateSaveButtonState();
    }

    public void setToken(String value) {
        token.setValue(value != null ? value : "");
        updateSaveButtonState();
    }

    public void setOpenClawAccount(String value) {
        openClawAccount.setValue(value != null ? value : "");
        updateSaveButtonState();
    }

    /**
     * 从本地存储加载现有配置
     */
    public void loadExistingConfig() {
        String savedAppKey = ConfigDataUtils.getAppKey(getApplication());
        String savedAccount = ConfigDataUtils.getAccount(getApplication());
        String savedToken = ConfigDataUtils.getToken(getApplication());
        String savedOpenClawAccount = ConfigDataUtils.getOpenClawAccount(getApplication());

        appKey.setValue(savedAppKey != null ? savedAppKey : "");
        account.setValue(savedAccount != null ? savedAccount : "");
        token.setValue(savedToken != null ? savedToken : "");
        openClawAccount.setValue(savedOpenClawAccount != null ? savedOpenClawAccount : "");
        
        updateSaveButtonState();
    }

    /**
     * 验证并保存配置
     * @return true if validation passes, false otherwise
     */
    public boolean validateAndSaveConfig() {
        String appKeyValue = appKey.getValue();
        String accountValue = account.getValue();
        String tokenValue = token.getValue();
        String openClawAccountValue = openClawAccount.getValue();

        // 验证必填字段
        if (TextUtils.isEmpty(appKeyValue)) {
            validationError.setValue("AppKey不能为空");
            return false;
        }

        if (TextUtils.isEmpty(accountValue)) {
            validationError.setValue("账号不能为空");
            return false;
        }

        if (TextUtils.isEmpty(tokenValue)) {
            validationError.setValue("Token不能为空");
            return false;
        }

        // 验证AppKey格式
        if (!isValidAppKey(appKeyValue)) {
            validationError.setValue("AppKey格式不正确，长度至少20位");
            return false;
        }

        // 保存配置
        try {
            ConfigDataUtils.saveAllConfig(
                getApplication(),
                appKeyValue,
                accountValue,
                tokenValue,
                openClawAccountValue
            );
            
            saveResult.setValue(new SaveResult(true, "保存成功"));
            return true;
        } catch (Exception e) {
            saveResult.setValue(new SaveResult(false, "保存失败：" + e.getMessage()));
            return false;
        }
    }

    /**
     * 重置配置到默认状态
     */
    public void resetConfig() {
        ConfigDataUtils.resetConfig(getApplication());
        appKey.setValue("");
        account.setValue("");
        token.setValue("");
        openClawAccount.setValue("");
        updateSaveButtonState();
    }

    /**
     * 验证AppKey格式
     * @param appKey 待验证的AppKey
     * @return true if valid, false otherwise
     */
    public boolean isValidAppKey(String appKey) {
        return ConfigDataUtils.isValidAppKey(appKey);
    }

    /**
     * 更新保存按钮状态
     */
    private void updateSaveButtonState() {
        String appKeyValue = appKey.getValue();
        String accountValue = account.getValue();
        String tokenValue = token.getValue();

        boolean isEnabled = !TextUtils.isEmpty(appKeyValue) 
            && !TextUtils.isEmpty(accountValue) 
            && !TextUtils.isEmpty(tokenValue)
            && isValidAppKey(appKeyValue);

        saveButtonEnabled.setValue(isEnabled);
    }

    /**
     * 保存结果封装类
     */
    public static class SaveResult {
        private final boolean success;
        private final String errorMessage;

        public SaveResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}