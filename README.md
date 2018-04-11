### WifiConfiguration

- A class representing a configured Wi-Fi network, including the security configuration

几个相关的内部类

> KeyMgmt：Recognized key management schemes（认证方式）

```java
/** WPA is not used; plaintext or static WEP could be used. */
public static final int NONE = 0;
/** WPA pre-shared key (requires {@code preSharedKey} to be specified). */
public static final int WPA_PSK = 1
/** WPA using EAP authentication. Generally used with an external authentication server. */
public static final int WPA_EAP = 2;
/* IEEE 802.1X using EAP authentication and (optionally) dynamically generated WEP keys. */
public static final int IEEE8021X = 3;
```

> AuthAlgorithm：Recognized IEEE 802.11 authentication algorithms.

```java
/** Open System authentication (required for WPA/WPA2) */
public static final int OPEN = 0;
/** Shared Key authentication (requires static WEP keys) */
public static final int SHARED = 1;
/** LEAP/Network EAP (only used with LEAP) */
public static final int LEAP = 2;
```

> Protocol：Recognized security protocols：加密方式

```java
/** WPA/IEEE 802.11i/D3.0 */
public static final int WPA = 0;  //WPA
/** WPA2/IEEE 802.11i */
public static final int RSN = 1;  //WPA2
```

> GroupCipher：Recognized group ciphers

```java
/** WEP40 = WEP (Wired Equivalent Privacy) with 40-bit key (original 802.11) */
public static final int WEP40 = 0;
/** WEP104 = WEP (Wired Equivalent Privacy) with 104-bit key */
public static final int WEP104 = 1;
/** Temporal Key Integrity Protocol [IEEE 802.11i/D7.0] */
public static final int TKIP = 2;
/** AES in Counter mode with CBC-MAC [RFC 3610, IEEE 802.11i/D7.0] */
public static final int CCMP = 3;
```

> Status：Possible status of a network configuration

```java
/** this is the network we are currently connected to */
public static final int CURRENT = 0;
/** supplicant will not attempt to use this network */
public static final int DISABLED = 1;
/** supplicant will consider this network available for association */
public static final int ENABLED = 2;
```



> PairwiseCipher：Recognized pairwise ciphers for WPA

```java
/** Use only Group keys (deprecated) */
public static final int NONE = 0;
/** Temporal Key Integrity Protocol [IEEE 802.11i/D7.0] */
public static final int TKIP = 1;
/** AES in Counter mode with CBC-MAC [RFC 3610, IEEE 802.11i/D7.0] */
public static final int CCMP = 2;
```



### 需要配置的成员变量

```java
    /**
     * The ID number that the supplicant uses to identify this
     * network configuration entry. This must be passed as an argument
     * to most calls into the supplicant.
     */
    public int networkId;

    /**
     * The current status of this network configuration entry.
     * Fixme We need remove this field to use only Quality network selection status only
     * @see Status
     */
    public int status;

    /**
     * The network's SSID. Can either be an ASCII string,
     * which must be enclosed in double quotation marks
     * (e.g., {@code "MyNetwork"}, or a string of
     * hex digits,which are not enclosed in quotes
     * (e.g., {@code 01a243f405}).
     */
    public String SSID;
    /**
     * When set, this network configuration entry should only be used when
     * associating with the AP having the specified BSSID. The value is
     * a string in the format of an Ethernet MAC address, e.g.,
     * <code>XX:XX:XX:XX:XX:XX</code> where each <code>X</code> is a hex digit.
     */
    public String BSSID;
    /**
     * Pre-shared key for use with WPA-PSK.
     * <p/>
     * When the value of this key is read, the actual key is
     * not returned, just a "*" if the key has a value, or the null
     * string otherwise.
     */
    public String preSharedKey;
    /**
     * Up to four WEP keys. Either an ASCII string enclosed in double
     * quotation marks (e.g., {@code "abcdef"} or a string
     * of hex digits (e.g., {@code 0102030405}).
     * <p/>
     * When the value of one of these keys is read, the actual key is
     * not returned, just a "*" if the key has a value, or the null
     * string otherwise.
     */
    public String[] wepKeys;

    /** Default WEP key index, ranging from 0 to 3. */
    public int wepTxKeyIndex;
	/**
     * The set of key management protocols supported by this configuration.
     * See {@link KeyMgmt} for descriptions of the values.
     * Defaults to WPA-PSK WPA-EAP.
     */
    public BitSet allowedKeyManagement;
    /**
     * The set of security protocols supported by this configuration.
     * See {@link Protocol} for descriptions of the values.
     * Defaults to WPA RSN.
     */
    public BitSet allowedProtocols;
    /**
     * The set of authentication protocols supported by this configuration.
     * See {@link AuthAlgorithm} for descriptions of the values.
     * Defaults to automatic selection.
     */
    public BitSet allowedAuthAlgorithms;
    /**
     * The set of pairwise ciphers for WPA supported by this configuration.
     * See {@link PairwiseCipher} for descriptions of the values.
     * Defaults to CCMP TKIP.
     */
    public BitSet allowedPairwiseCiphers;
    /**
     * The set of group ciphers supported by this configuration.
     * See {@link GroupCipher} for descriptions of the values.
     * Defaults to CCMP TKIP WEP104 WEP40.
     */
    public BitSet allowedGroupCiphers;
```

------

### 无线传输安全解决方案

首先需要明确的一点，无线局域网的标准是802.11，而WEP和WPA都是针对该标准制定的加密机制，也是我们日常接触的最多的，而对于EAP加密机制是针对企业级别；

不管是什么加密机制，它都有对应的认证方式和传输数据加密方式，来增强无线网络的安全性：

1、 认证机制：认证机制用来对用户的身份进行验证，以限定特定的用户（授权的用户）可以使用网络资源；

2、 加密机制：加密机制用来对无线链路的数据进行加密，以保证无线网络数据只被所期望的用户接收和理解；

对于无线传输，我们肯定看中的安全与否，网上有很多称呼“加密方式”、“加密类型”，搞的人很懵逼，这不是一个说辞嘛~

那么对于无线措施，我们肯定要采取一些保护我们的传输安全，目前有这样几种解决方案，来保证无线传输：

- WEP（Wired Equivalent Privacy）
  - 认证方式
    - 开放式系统认证（open system authentication）
    - 共有键认证（shared key authentication）
  - 加密方式
    - WEP：有限等效加密，全网都是一个加密方式，说的通俗点就是，你家传输数据的加密方式和我家传输数据的使用同一秘钥加密；
- WPA（Wi-Fi Protected Access）
  - 认证方式：802.1x+EAP（需要认证服务器）
  - 加密方式：TKIP+MIC
- WPA-PSK（Wi-Fi Protected Access-pre-shared key）
  - 认证方式：预共享秘钥，使每一个WLAN节点输入相同的秘钥即可
  - 加密方式：TKIP+MIC
- WPA2（Wi-Fi Protected Access）
  - 认证方式：802.1x+EAP（需要认证服务器）
  - 加密方式：AES/TKIP+CCMP
- WPA2-PSK（Wi-Fi Protected Access-pre-shared key）
  - 认证方式：预共享秘钥，使每一个WLAN节点输入相同的秘钥即可
  - 加密方式：AES/TKIP+CCMP

```
TKIP：Temporal Key Integrity Protocol 临时密钥完整性协议；
AES：Advanced Encryption Standard 高级加密标准；
MIC：Michael算法的消息认证码（在WPA中叫做消息完整性查核，简称MIC）；
CCMP：Temporal Key Integrity Protocol 临时密钥完整性协议；
```

```
802.1x + EAP，Pre-shared Key是身份校验算法（WEP没有设置有身份验证机制）
TKIP和AES是数据传输加密算法（类似于WEP加密的RC4 算法）
MIC和CCMP数据完整性编码校验算法（类似于WEP中CRC32算法）
```

**从上面的表述我们可以发现，WPA之于WPA2，就是加密方式的不同，从WPA的TKIP->AES，MIC->CCMP**

------

### 常用WI-FI模式

WIFI的模式有很多种，在这只记录两种，STA模式和AP模式：

公司目前使用AP配网和SmartConfig配网是就是针对这两种模式的应用场景，

- STA模式：任何一种无线网卡都可以运行在此模式下，这种模式也可以称为默认模式。在此模式下，无线网卡发送连接与认证消息给热点，热点接收到后完成认证后，发回成功认证消息，此网卡接入无线网络。这种模式下，wifi工作于从模式 ；
- AP模式(AccessPoint)：在一个无线网络环境中，无线热点是作为一个主设备，工作于主模式（Master mode）。通过管理控制可控制的STA，从而组成无线网络，也有相应的安全控制策略。由AP形成的网络，由AP的MAC地址唯一识别。热点完成创建后，会由热点创建一个被别的设备可识别的名称，称为SSID。