.class public Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;
.super Ljava/lang/Object;
.source "AKAAuthProviderAndroidImpl.java"

# interfaces
.implements Ljavax/microedition/ims/core/auth/AKAAuthProvider;


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;
    }
.end annotation


# static fields
.field private static final LOG_TAG:Ljava/lang/String; = "AKAAuthProviderAndroidImpl"

.field private static final akaResponseCache:Ljava/util/Map;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Map",
            "<",
            "Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;",
            "Ljavax/microedition/ims/core/auth/AKAResponse;",
            ">;"
        }
    .end annotation
.end field


# instance fields
.field private final manager:Landroid/telephony/TelephonyManager;

.field private telephonyHdl:Lcom/android/internal/telephony/ITelephony;


# direct methods
.method static constructor <clinit>()V
    .registers 1

    .prologue
    .line 88
    new-instance v0, Ljava/util/HashMap;

    invoke-direct {v0}, Ljava/util/HashMap;-><init>()V

    sput-object v0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->akaResponseCache:Ljava/util/Map;

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;)V
    .registers 5
    .parameter "context"

    .prologue
    .line 146
    invoke-direct/range {p0 .. p0}, Ljava/lang/Object;-><init>()V

    .line 86
    const/4 v0, 0x0

    iput-object v0, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->telephonyHdl:Lcom/android/internal/telephony/ITelephony;

    .line 147
    const-string v0, "phone"

    invoke-virtual {p1, v0}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Landroid/telephony/TelephonyManager;

    iput-object v0, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->manager:Landroid/telephony/TelephonyManager;

    .line 148
    iget-object v0, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->manager:Landroid/telephony/TelephonyManager;

    if-nez v0, :cond_2d

    .line 149
    new-instance v0, Ljava/lang/IllegalArgumentException;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "Invalid Android context. Can not obtain telephony manager. Context = "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-direct {v0, v1}, Ljava/lang/IllegalArgumentException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 151
    :cond_2d
    return-void
.end method

.method private doCalculateAkaResponse([B[B)Ljavax/microedition/ims/core/auth/AKAResponse;
    .registers 20
    .parameter "rand"
    .parameter "autn"
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljavax/microedition/ims/core/auth/AuthCalculationException;
        }
    .end annotation

    .prologue
    .line 205
    const-string v14, "AKAAuthProviderAndroidImpl"

    new-instance v15, Ljava/lang/StringBuilder;

    invoke-direct {v15}, Ljava/lang/StringBuilder;-><init>()V

    const-string v16, "IsimAid() "

    invoke-virtual/range {v15 .. v16}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v15

    move-object/from16 v0, p0

    iget-object v0, v0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->manager:Landroid/telephony/TelephonyManager;

    move-object/from16 v16, v0

    invoke-virtual/range {v16 .. v16}, Landroid/telephony/TelephonyManager;->getIsimAid()Ljava/lang/String;

    move-result-object v16

    invoke-virtual/range {v15 .. v16}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v15

    invoke-virtual {v15}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v15

    invoke-static {v14, v15}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 208
    const/4 v11, 0x0

    .line 209
    .local v11, res:[B
    const/4 v2, 0x0

    .line 210
    .local v2, Ck:[B
    const/4 v3, 0x0

    .line 211
    .local v3, Ik:[B
    const/4 v10, 0x0

    .line 212
    .local v10, kc:[B
    const/4 v4, 0x0

    .line 215
    .local v4, auts:[B
    :try_start_27
    invoke-direct/range {p0 .. p0}, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->getTelephonyHdl()Lcom/android/internal/telephony/ITelephony;

    move-result-object v14

    move-object/from16 v0, p0

    iget-object v15, v0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->manager:Landroid/telephony/TelephonyManager;

    invoke-virtual {v15}, Landroid/telephony/TelephonyManager;->getIsimAid()Ljava/lang/String;

    move-result-object v15

    invoke-interface {v14, v15}, Lcom/android/internal/telephony/ITelephony;->openIccLogicalChannel(Ljava/lang/String;)I

    move-result v8

    .line 217
    .local v8, iChannelId:I
    const/4 v14, -0x1

    if-ne v8, v14, :cond_43

    .line 218
    const-string v14, "AKAAuthProviderAndroidImpl"

    const-string v15, "unable to open logical channel"

    invoke-static {v14, v15}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    .line 219
    const/4 v14, 0x0

    .line 303
    .end local v8           #iChannelId:I
    :goto_42
    return-object v14

    .line 222
    .restart local v8       #iChannelId:I
    :cond_43
    const-string v14, "AKAAuthProviderAndroidImpl"

    new-instance v15, Ljava/lang/StringBuilder;

    invoke-direct {v15}, Ljava/lang/StringBuilder;-><init>()V

    const-string v16, "iChannelId() "

    invoke-virtual/range {v15 .. v16}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v15

    invoke-virtual {v15, v8}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v15

    invoke-virtual {v15}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v15

    invoke-static {v14, v15}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 223
    move-object/from16 v0, p0

    iget-object v14, v0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->manager:Landroid/telephony/TelephonyManager;

    move-object/from16 v0, p1

    move-object/from16 v1, p2

    invoke-virtual {v14, v0, v1}, Landroid/telephony/TelephonyManager;->calculateAkaResponse([B[B)Ljava/lang/String;

    move-result-object v14

    move-object/from16 v0, p0

    invoke-direct {v0, v14}, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->hexStringToBytes(Ljava/lang/String;)[B

    move-result-object v13

    .line 224
    .local v13, result:[B
    const-string v14, "AKAAuthProviderAndroidImpl"

    new-instance v15, Ljava/lang/StringBuilder;

    invoke-direct {v15}, Ljava/lang/StringBuilder;-><init>()V

    const-string v16, "calculateAkaResponse() "

    invoke-virtual/range {v15 .. v16}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v15

    invoke-virtual {v15, v13}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v15

    invoke-virtual {v15}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v15

    invoke-static {v14, v15}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 226
    invoke-direct/range {p0 .. p0}, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->getTelephonyHdl()Lcom/android/internal/telephony/ITelephony;

    move-result-object v14

    invoke-interface {v14, v8}, Lcom/android/internal/telephony/ITelephony;->closeIccLogicalChannel(I)Z

    .line 228
    const/4 v14, 0x0

    aget-byte v14, v13, v14

    const/16 v15, -0x25

    if-eq v14, v15, :cond_98

    const/4 v14, 0x0

    aget-byte v14, v13, v14

    if-nez v14, :cond_ea

    .line 251
    :cond_98
    const/4 v14, 0x1

    aget-byte v12, v13, v14

    .line 252
    .local v12, reslen:I
    if-lez v12, :cond_a4

    .line 253
    new-array v11, v12, [B

    .line 254
    const/4 v14, 0x2

    const/4 v15, 0x0

    invoke-static {v13, v14, v11, v15, v12}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V

    .line 257
    :cond_a4
    add-int/lit8 v14, v12, 0x2

    aget-byte v6, v13, v14

    .line 258
    .local v6, cklen:I
    if-lez v6, :cond_b2

    .line 259
    new-array v2, v6, [B

    .line 260
    add-int/lit8 v14, v12, 0x3

    const/4 v15, 0x0

    invoke-static {v13, v14, v2, v15, v6}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V

    .line 262
    :cond_b2
    add-int/lit8 v14, v12, 0x3

    add-int/2addr v14, v6

    aget-byte v9, v13, v14

    .line 263
    .local v9, iklen:I
    if-lez v9, :cond_c2

    .line 264
    new-array v3, v9, [B

    .line 265
    add-int/lit8 v14, v12, 0x4

    add-int/2addr v14, v6

    const/4 v15, 0x0

    invoke-static {v13, v14, v3, v15, v9}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V
    :try_end_c2
    .catch Landroid/os/RemoteException; {:try_start_27 .. :try_end_c2} :catch_fe
    .catch Ljava/lang/Exception; {:try_start_27 .. :try_end_c2} :catch_11b

    .line 298
    .end local v6           #cklen:I
    .end local v8           #iChannelId:I
    .end local v9           #iklen:I
    .end local v12           #reslen:I
    .end local v13           #result:[B
    :cond_c2
    :goto_c2
    if-nez v11, :cond_138

    .line 299
    const-string v14, "AKAAuthProviderAndroidImpl"

    new-instance v15, Ljava/lang/StringBuilder;

    invoke-direct {v15}, Ljava/lang/StringBuilder;-><init>()V

    const-string v16, "telephonyManager = "

    invoke-virtual/range {v15 .. v16}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v15

    move-object/from16 v0, p0

    iget-object v0, v0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->manager:Landroid/telephony/TelephonyManager;

    move-object/from16 v16, v0

    invoke-virtual/range {v15 .. v16}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v15

    invoke-virtual {v15}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v15

    invoke-static {v14, v15}, Ljavax/microedition/ims/common/Logger;->log(Ljava/lang/String;Ljava/lang/String;)V

    .line 300
    new-instance v14, Ljavax/microedition/ims/core/auth/AuthCalculationException;

    const-string v15, "Aka response can\'t be calculated"

    invoke-direct {v14, v15}, Ljavax/microedition/ims/core/auth/AuthCalculationException;-><init>(Ljava/lang/String;)V

    throw v14

    .line 268
    .restart local v8       #iChannelId:I
    .restart local v13       #result:[B
    :cond_ea
    const/4 v14, 0x0

    :try_start_eb
    aget-byte v14, v13, v14

    const/16 v15, -0x24

    if-ne v14, v15, :cond_c2

    .line 282
    const/4 v14, 0x1

    aget-byte v5, v13, v14

    .line 283
    .local v5, autslen:I
    if-lez v5, :cond_c2

    .line 284
    new-array v4, v5, [B

    .line 285
    const/4 v14, 0x2

    const/4 v15, 0x0

    invoke-static {v13, v14, v4, v15, v5}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V
    :try_end_fd
    .catch Landroid/os/RemoteException; {:try_start_eb .. :try_end_fd} :catch_fe
    .catch Ljava/lang/Exception; {:try_start_eb .. :try_end_fd} :catch_11b

    goto :goto_c2

    .line 288
    .end local v5           #autslen:I
    .end local v8           #iChannelId:I
    .end local v13           #result:[B
    :catch_fe
    move-exception v7

    .line 290
    .local v7, e:Landroid/os/RemoteException;
    invoke-virtual {v7}, Landroid/os/RemoteException;->printStackTrace()V

    .line 291
    const-string v14, "AKAAuthProviderAndroidImpl"

    new-instance v15, Ljava/lang/StringBuilder;

    invoke-direct {v15}, Ljava/lang/StringBuilder;-><init>()V

    const-string v16, "error1:"

    invoke-virtual/range {v15 .. v16}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v15

    invoke-virtual {v15, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v15

    invoke-virtual {v15}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v15

    invoke-static {v14, v15}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_c2

    .line 292
    .end local v7           #e:Landroid/os/RemoteException;
    :catch_11b
    move-exception v7

    .line 294
    .local v7, e:Ljava/lang/Exception;
    invoke-virtual {v7}, Ljava/lang/Exception;->printStackTrace()V

    .line 295
    const-string v14, "AKAAuthProviderAndroidImpl"

    new-instance v15, Ljava/lang/StringBuilder;

    invoke-direct {v15}, Ljava/lang/StringBuilder;-><init>()V

    const-string v16, "error2:"

    invoke-virtual/range {v15 .. v16}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v15

    invoke-virtual {v15, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v15

    invoke-virtual {v15}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v15

    invoke-static {v14, v15}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_c2

    .line 303
    .end local v7           #e:Ljava/lang/Exception;
    :cond_138
    if-nez v11, :cond_13d

    const/4 v14, 0x0

    goto/16 :goto_42

    :cond_13d
    new-instance v14, Ljavax/microedition/ims/core/auth/AKAResponseImpl;

    invoke-direct {v14, v2, v3, v4, v11}, Ljavax/microedition/ims/core/auth/AKAResponseImpl;-><init>([B[B[B[B)V

    goto/16 :goto_42
.end method

.method private getTelephonyHdl()Lcom/android/internal/telephony/ITelephony;
    .registers 4
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/Exception;
        }
    .end annotation

    .prologue
    .line 91
    iget-object v2, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->telephonyHdl:Lcom/android/internal/telephony/ITelephony;

    if-nez v2, :cond_10

    .line 92
    const-string v1, "phone"

    .line 93
    .local v1, service:Ljava/lang/String;
    invoke-static {v1}, Landroid/os/ServiceManager;->getService(Ljava/lang/String;)Landroid/os/IBinder;

    move-result-object v0

    .line 94
    .local v0, binder:Landroid/os/IBinder;
    invoke-static {v0}, Lcom/android/internal/telephony/ITelephony$Stub;->asInterface(Landroid/os/IBinder;)Lcom/android/internal/telephony/ITelephony;

    move-result-object v2

    iput-object v2, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->telephonyHdl:Lcom/android/internal/telephony/ITelephony;

    .line 95
    .end local v0           #binder:Landroid/os/IBinder;
    .end local v1           #service:Ljava/lang/String;
    :cond_10
    iget-object v2, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->telephonyHdl:Lcom/android/internal/telephony/ITelephony;

    return-object v2
.end method

.method private hexCharToInt(C)I
    .registers 5
    .parameter "c"

    .prologue
    .line 197
    const/16 v0, 0x30

    if-lt p1, v0, :cond_b

    const/16 v0, 0x39

    if-gt p1, v0, :cond_b

    add-int/lit8 v0, p1, -0x30

    .line 199
    :goto_a
    return v0

    .line 198
    :cond_b
    const/16 v0, 0x41

    if-lt p1, v0, :cond_18

    const/16 v0, 0x46

    if-gt p1, v0, :cond_18

    add-int/lit8 v0, p1, -0x41

    add-int/lit8 v0, v0, 0xa

    goto :goto_a

    .line 199
    :cond_18
    const/16 v0, 0x61

    if-lt p1, v0, :cond_25

    const/16 v0, 0x66

    if-gt p1, v0, :cond_25

    add-int/lit8 v0, p1, -0x61

    add-int/lit8 v0, v0, 0xa

    goto :goto_a

    .line 201
    :cond_25
    new-instance v0, Ljava/lang/RuntimeException;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "invalid hex char \'"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, "\'"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-direct {v0, v1}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V

    throw v0
.end method

.method private hexStringToBytes(Ljava/lang/String;)[B
    .registers 8
    .parameter "s"

    .prologue
    .line 177
    if-nez p1, :cond_4

    const/4 v1, 0x0

    .line 188
    :cond_3
    return-object v1

    .line 179
    :cond_4
    invoke-virtual {p1}, Ljava/lang/String;->length()I

    move-result v2

    .line 181
    .local v2, sz:I
    div-int/lit8 v3, v2, 0x2

    new-array v1, v3, [B

    .line 183
    .local v1, ret:[B
    const/4 v0, 0x0

    .local v0, i:I
    :goto_d
    if-ge v0, v2, :cond_3

    .line 184
    div-int/lit8 v3, v0, 0x2

    invoke-virtual {p1, v0}, Ljava/lang/String;->charAt(I)C

    move-result v4

    invoke-direct {p0, v4}, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->hexCharToInt(C)I

    move-result v4

    shl-int/lit8 v4, v4, 0x4

    add-int/lit8 v5, v0, 0x1

    invoke-virtual {p1, v5}, Ljava/lang/String;->charAt(I)C

    move-result v5

    invoke-direct {p0, v5}, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->hexCharToInt(C)I

    move-result v5

    or-int/2addr v4, v5

    int-to-byte v4, v4

    aput-byte v4, v1, v3

    .line 183
    add-int/lit8 v0, v0, 0x2

    goto :goto_d
.end method


# virtual methods
.method public calculateAkaResponse([B[B)Ljavax/microedition/ims/core/auth/AKAResponse;
    .registers 8
    .parameter "rand"
    .parameter "autn"
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljavax/microedition/ims/core/auth/AuthCalculationException;
        }
    .end annotation

    .prologue
    .line 157
    new-instance v0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;

    invoke-direct {v0, p0, p1, p2}, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;-><init>(Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;[B[B)V

    .line 159
    .local v0, akaResponseKey:Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;
    sget-object v2, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->akaResponseCache:Ljava/util/Map;

    invoke-interface {v2, v0}, Ljava/util/Map;->containsKey(Ljava/lang/Object;)Z

    move-result v2

    if-eqz v2, :cond_35

    .line 160
    const-string v2, "AKAAuthProviderAndroidImpl"

    const-string v3, "calculateAkaResponse#aka response has been calculated, retrieved from cache"

    invoke-static {v2, v3}, Ljavax/microedition/ims/common/Logger;->log(Ljava/lang/String;Ljava/lang/String;)V

    .line 161
    sget-object v2, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->akaResponseCache:Ljava/util/Map;

    invoke-interface {v2, v0}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Ljavax/microedition/ims/core/auth/AKAResponse;

    .line 168
    .local v1, retValue:Ljavax/microedition/ims/core/auth/AKAResponse;
    :goto_1c
    sget-object v2, Ljavax/microedition/ims/common/Logger$Tag;->COMMON:Ljavax/microedition/ims/common/Logger$Tag;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "androidAKAResponse = "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v2, v3}, Ljavax/microedition/ims/common/Logger;->log(Ljavax/microedition/ims/common/Logger$Tag;Ljava/lang/String;)V

    .line 170
    return-object v1

    .line 163
    .end local v1           #retValue:Ljavax/microedition/ims/core/auth/AKAResponse;
    :cond_35
    const-string v2, "AKAAuthProviderAndroidImpl"

    const-string v3, "calculateAkaResponse#aka response hasn\'t been calculated yet, retrieved from telephony manager"

    invoke-static {v2, v3}, Ljavax/microedition/ims/common/Logger;->log(Ljava/lang/String;Ljava/lang/String;)V

    .line 164
    invoke-direct {p0, p1, p2}, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->doCalculateAkaResponse([B[B)Ljavax/microedition/ims/core/auth/AKAResponse;

    move-result-object v1

    .line 165
    .restart local v1       #retValue:Ljavax/microedition/ims/core/auth/AKAResponse;
    sget-object v2, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->akaResponseCache:Ljava/util/Map;

    invoke-interface {v2, v0, v1}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    goto :goto_1c
.end method

.method public getHomeNetworkDomain()Ljava/lang/String;
    .registers 2

    .prologue
    .line 192
    iget-object v0, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->manager:Landroid/telephony/TelephonyManager;

    invoke-virtual {v0}, Landroid/telephony/TelephonyManager;->getIsimDomain()Ljava/lang/String;

    move-result-object v0

    return-object v0
.end method

.method public getImpi()Ljavax/microedition/ims/config/UserInfo;
    .registers 4

    .prologue
    .line 308
    sget-object v0, Ljavax/microedition/ims/common/Logger$Tag;->COMMON:Ljavax/microedition/ims/common/Logger$Tag;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "Device impi: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    iget-object v2, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->manager:Landroid/telephony/TelephonyManager;

    invoke-virtual {v2}, Landroid/telephony/TelephonyManager;->getIsimImpi()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Ljavax/microedition/ims/common/Logger;->log(Ljavax/microedition/ims/common/Logger$Tag;Ljava/lang/String;)V

    .line 309
    iget-object v0, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->manager:Landroid/telephony/TelephonyManager;

    invoke-virtual {v0}, Landroid/telephony/TelephonyManager;->getIsimImpi()Ljava/lang/String;

    move-result-object v0

    invoke-static {v0}, Ljavax/microedition/ims/config/UserInfo;->valueOf(Ljava/lang/String;)Ljavax/microedition/ims/config/UserInfo;

    move-result-object v0

    return-object v0
.end method

.method public getImpu()Ljavax/microedition/ims/config/UserInfo;
    .registers 6

    .prologue
    .line 316
    iget-object v2, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->manager:Landroid/telephony/TelephonyManager;

    invoke-virtual {v2}, Landroid/telephony/TelephonyManager;->getIsimImpu()[Ljava/lang/String;

    move-result-object v0

    .line 317
    .local v0, impu:[Ljava/lang/String;
    sget-object v2, Ljavax/microedition/ims/common/Logger$Tag;->COMMON:Ljavax/microedition/ims/common/Logger$Tag;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "getImpu#Device impu: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-static {v0}, Ljava/util/Arrays;->toString([Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v4

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v2, v3}, Ljavax/microedition/ims/common/Logger;->log(Ljavax/microedition/ims/common/Logger$Tag;Ljava/lang/String;)V

    .line 319
    if-eqz v0, :cond_47

    array-length v2, v0

    if-lez v2, :cond_47

    const/4 v2, 0x0

    aget-object v2, v0, v2

    invoke-static {v2}, Ljavax/microedition/ims/config/UserInfo;->valueOf(Ljava/lang/String;)Ljavax/microedition/ims/config/UserInfo;

    move-result-object v1

    .line 321
    .local v1, retValue:Ljavax/microedition/ims/config/UserInfo;
    :goto_2e
    sget-object v2, Ljavax/microedition/ims/common/Logger$Tag;->COMMON:Ljavax/microedition/ims/common/Logger$Tag;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "getImpu#retValue: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v2, v3}, Ljavax/microedition/ims/common/Logger;->log(Ljavax/microedition/ims/common/Logger$Tag;Ljava/lang/String;)V

    .line 323
    return-object v1

    .line 319
    .end local v1           #retValue:Ljavax/microedition/ims/config/UserInfo;
    :cond_47
    const/4 v1, 0x0

    goto :goto_2e
.end method

.method public isGbaUSupported()Z
    .registers 4

    .prologue
    .line 327
    sget-object v0, Ljavax/microedition/ims/common/Logger$Tag;->COMMON:Ljavax/microedition/ims/common/Logger$Tag;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "isGbaUSupported: manager.hasIccCard():"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    iget-object v2, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->manager:Landroid/telephony/TelephonyManager;

    invoke-virtual {v2}, Landroid/telephony/TelephonyManager;->hasIccCard()Z

    move-result v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Ljavax/microedition/ims/common/Logger;->log(Ljavax/microedition/ims/common/Logger$Tag;Ljava/lang/String;)V

    .line 328
    sget-object v0, Ljavax/microedition/ims/common/Logger$Tag;->COMMON:Ljavax/microedition/ims/common/Logger$Tag;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "isGbaUSupported: manager.hasIsim():"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    iget-object v2, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->manager:Landroid/telephony/TelephonyManager;

    invoke-virtual {v2}, Landroid/telephony/TelephonyManager;->hasIsim()Z

    move-result v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Ljavax/microedition/ims/common/Logger;->log(Ljavax/microedition/ims/common/Logger$Tag;Ljava/lang/String;)V

    .line 329
    sget-object v0, Ljavax/microedition/ims/common/Logger$Tag;->COMMON:Ljavax/microedition/ims/common/Logger$Tag;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "isGbaUSupported: manager.isGbaSupported():"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    iget-object v2, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->manager:Landroid/telephony/TelephonyManager;

    invoke-virtual {v2}, Landroid/telephony/TelephonyManager;->isGbaSupported()Z

    move-result v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Ljavax/microedition/ims/common/Logger;->log(Ljavax/microedition/ims/common/Logger$Tag;Ljava/lang/String;)V

    .line 332
    iget-object v0, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->manager:Landroid/telephony/TelephonyManager;

    invoke-virtual {v0}, Landroid/telephony/TelephonyManager;->hasIsim()Z

    move-result v0

    if-eqz v0, :cond_6c

    iget-object v0, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;->manager:Landroid/telephony/TelephonyManager;

    invoke-virtual {v0}, Landroid/telephony/TelephonyManager;->isGbaSupported()Z

    move-result v0

    if-eqz v0, :cond_6c

    const/4 v0, 0x1

    :goto_6b
    return v0

    :cond_6c
    const/4 v0, 0x0

    goto :goto_6b
.end method
