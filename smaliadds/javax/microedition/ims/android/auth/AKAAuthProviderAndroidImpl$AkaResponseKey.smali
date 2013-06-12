.class final Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;
.super Ljava/lang/Object;
.source "AKAAuthProviderAndroidImpl.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x12
    name = "AkaResponseKey"
.end annotation


# instance fields
.field private final autn:[B

.field private final rand:[B

.field final synthetic this$0:Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;


# direct methods
.method public constructor <init>(Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;[B[B)V
    .registers 4
    .parameter
    .parameter "rand"
    .parameter "autn"

    .prologue
    .line 102
    iput-object p1, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;->this$0:Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;

    invoke-direct/range {p0 .. p0}, Ljava/lang/Object;-><init>()V

    .line 103
    iput-object p2, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;->rand:[B

    .line 104
    iput-object p3, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;->autn:[B

    .line 105
    return-void
.end method

.method private getOuterType()Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;
    .registers 2

    .prologue
    .line 136
    iget-object v0, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;->this$0:Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;

    return-object v0
.end method


# virtual methods
.method public equals(Ljava/lang/Object;)Z
    .registers 7
    .parameter "obj"

    .prologue
    const/4 v1, 0x1

    const/4 v2, 0x0

    .line 119
    if-ne p0, p1, :cond_5

    .line 132
    :cond_4
    :goto_4
    return v1

    .line 121
    :cond_5
    if-nez p1, :cond_9

    move v1, v2

    .line 122
    goto :goto_4

    .line 123
    :cond_9
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v3

    invoke-virtual {p1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v4

    if-eq v3, v4, :cond_15

    move v1, v2

    .line 124
    goto :goto_4

    :cond_15
    move-object v0, p1

    .line 125
    check-cast v0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;

    .line 126
    .local v0, other:Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;
    invoke-direct {p0}, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;->getOuterType()Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;

    move-result-object v3

    invoke-direct {v0}, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;->getOuterType()Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;

    move-result-object v4

    invoke-virtual {v3, v4}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    move-result v3

    if-nez v3, :cond_28

    move v1, v2

    .line 127
    goto :goto_4

    .line 128
    :cond_28
    iget-object v3, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;->autn:[B

    iget-object v4, v0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;->autn:[B

    invoke-static {v3, v4}, Ljava/util/Arrays;->equals([B[B)Z

    move-result v3

    if-nez v3, :cond_34

    move v1, v2

    .line 129
    goto :goto_4

    .line 130
    :cond_34
    iget-object v3, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;->rand:[B

    iget-object v4, v0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;->rand:[B

    invoke-static {v3, v4}, Ljava/util/Arrays;->equals([B[B)Z

    move-result v3

    if-nez v3, :cond_4

    move v1, v2

    .line 131
    goto :goto_4
.end method

.method public hashCode()I
    .registers 5

    .prologue
    .line 109
    const/16 v0, 0x1f

    .line 110
    .local v0, prime:I
    const/4 v1, 0x1

    .line 111
    .local v1, result:I
    invoke-direct {p0}, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;->getOuterType()Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/Object;->hashCode()I

    move-result v2

    add-int/lit8 v1, v2, 0x1f

    .line 112
    mul-int/lit8 v2, v1, 0x1f

    iget-object v3, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;->autn:[B

    invoke-static {v3}, Ljava/util/Arrays;->hashCode([B)I

    move-result v3

    add-int v1, v2, v3

    .line 113
    mul-int/lit8 v2, v1, 0x1f

    iget-object v3, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;->rand:[B

    invoke-static {v3}, Ljava/util/Arrays;->hashCode([B)I

    move-result v3

    add-int v1, v2, v3

    .line 114
    return v1
.end method

.method public toString()Ljava/lang/String;
    .registers 3

    .prologue
    .line 141
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "AkaResponseKey [rand="

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    iget-object v1, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;->rand:[B

    invoke-static {v1}, Ljava/util/Arrays;->toString([B)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    const-string v1, ", autn="

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    iget-object v1, p0, Ljavax/microedition/ims/android/auth/AKAAuthProviderAndroidImpl$AkaResponseKey;->autn:[B

    invoke-static {v1}, Ljava/util/Arrays;->toString([B)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    const-string v1, "]"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    return-object v0
.end method
