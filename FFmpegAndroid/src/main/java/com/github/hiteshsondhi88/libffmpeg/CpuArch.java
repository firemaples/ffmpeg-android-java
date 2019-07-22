package com.github.hiteshsondhi88.libffmpeg;

import android.text.TextUtils;

enum CpuArch {
    x86("e2e067a2faf5d5ec37f27453825e064a9b8829cc"),
    x86_64("4a8b5cb64b56d36bc87193fe4aa890177716ac76"),
    ARMv7("0e08dc7ddd8cb09c42a8c88cdb9b56e0a81502e0"),
    ARM64("46ce41d0696341668244b3f90c4da0cb6d4af36f"),
    NONE(null);

    private String sha1;

    CpuArch(String sha1) {
        this.sha1 = sha1;
    }

    String getSha1() {
        return sha1;
    }

    static CpuArch fromString(String sha1) {
        if (!TextUtils.isEmpty(sha1)) {
            for (CpuArch cpuArch : CpuArch.values()) {
                if (sha1.equalsIgnoreCase(cpuArch.sha1)) {
                    return cpuArch;
                }
            }
        }
        return NONE;
    }
}
