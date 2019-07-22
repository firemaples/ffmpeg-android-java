package com.github.hiteshsondhi88.libffmpeg;

import android.os.Build;

import java.util.Arrays;

class CpuArchHelper {

    static CpuArch getCpuArch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d("Build.SUPPORTED_ABIS: " + Arrays.toString(Build.SUPPORTED_ABIS));
            for (String abi : Build.SUPPORTED_ABIS) {
                CpuArch cpuArch = getCpuArch(abi);
                if (cpuArch != CpuArch.NONE) {
                    return cpuArch;
                }
            }
        }

        Log.d("Build.CPU_ABI : " + Build.CPU_ABI);
        return getCpuArch(Build.CPU_ABI);
    }

    static CpuArch getCpuArch(String abi) {
        if (getx86CpuAbi().equals(abi)) {
            return CpuArch.x86;
        } else if (getx86_64CpuAbi().equals(abi)) {
            return CpuArch.x86_64;
        } else if (getArmeabiv7CpuAbi().equals(abi)) {
            ArmArchHelper cpuNativeArchHelper = new ArmArchHelper();
            String archInfo = cpuNativeArchHelper.cpuArchFromJNI();
            // check if device is arm v7
            if (cpuNativeArchHelper.isARM_v7_CPU(archInfo)) {
                // check if device is neon
                return CpuArch.ARMv7;
            }
        } else if (getArm64CpuAbi().equals(abi)) {
            return CpuArch.ARM64;
        }
        return CpuArch.NONE;
    }

    static String getx86CpuAbi() {
        return "x86";
    }

    static String getx86_64CpuAbi() {
        return "x86_64";
    }

    static String getArm64CpuAbi() {
        return "arm64-v8a";
    }

    static String getArmeabiv7CpuAbi() {
        return "armeabi-v7a";
    }
}
