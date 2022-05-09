/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.app.im.main.mine.setting;

import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.corekit.im.repo.ConfigRepo;

public class SettingViewModel extends BaseViewModel {

    public boolean getDeleteAlias(){
       return ConfigRepo.getDeleteAlias();
    }

    public void setDeleteAlias(boolean delete){
        ConfigRepo.updateDeleteAlias(delete);
    }

    public boolean getShowReadStatus(){
        return ConfigRepo.getShowReadStatus();
    }

    public void setShowReadStatus(boolean delete){
        ConfigRepo.updateShowReadStatus(delete);
    }

    public int getAudioPlayMode(){
        return ConfigRepo.getAudioPlayModel();
    }

    public void setAudioPlayMode(int mode){
        ConfigRepo.updateAudioPlayMode(mode);
    }

}
