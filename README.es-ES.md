**QsBle es un framework Ble para Android**

[中文文档](https://github.com/zqisheng/QsBle/blob/master/README_CN.md)

[Documento en inglés](https://github.com/zqisheng/QsBle/blob/master/README.md)

[Haz clic para descargar la demo](https://raw.githubusercontent.com/zqisheng/QsBle/master/screen/app-debug.apk)

**Características**
* En comparación con el antiguo framework Ble, se ajusta más al estilo de código de los desarrolladores actuales de Android
* Programación funcional completa, todas las devoluciones de llamada Ble son funcionales
* Admite devoluciones de llamada para todas las operaciones Ble
* Cuando los datos enviados superan el mtu, QsBle también tiene un mecanismo perfecto de agrupación de paquetes
* Admite desconexión y reconexión automática
* Admite corrutinas de Kotlin, permitiéndote operar código asíncrono de manera síncrona sin preocuparte por los diversos infiernos de devoluciones de llamada del Bluetooth
* Admite Flow
* Admite programación en cadena, permitiéndote realizar varias operaciones en Bluetooth secuencialmente. La lógica que antes solo se podía implementar en cientos de líneas de código, ahora con QsBle solo necesita una línea de código
* Soporte perfecto para ota, solo necesitas proporcionar tu archivo ota, todas las demás operaciones se entregan a QsBle
* El mecanismo perfecto de manejo de excepciones te permite localizar el problema a tiempo cuando ocurre una excepción.
* El código principal está escrito en Java, y las personas que desarrollan en Java no necesitan preocuparse por la incapacidad de usar Java, y las llamadas de Kotlin a código Java también proporcionan varios mecanismos de nulabilidad, para que no tengas que preocuparte por el puntero nulo de Kotlin al llamar código Java
* Otros frameworks Ble tienen algunas cosas, y QsBle también las tiene

### Agregar dependencias
[![](https://jitpack.io/v/zqisheng/QsBle.svg)](https://jitpack.io/#zqisheng/QsBle)
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }


dependencies {
    //Dependencias que QsBle debe agregar
   implementation 'com.github.zqisheng.QsBle:ble:1.2.6
    //Si deseas usar la función de corrutinas de kotlin, debes agregar las siguientes dependencias, no es obligatorio
   //implementation 'com.github.zqisheng.QsBle:ble_coroutines:1.2.6
}
```

### Antes de usar
Agregar permisos para el escaneo Bluetooth necesita ser añadido, y solicitar permisos en tiempo de ejecución
```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```
Para la adaptación Bluetooth de Android 12, se han añadido los siguientes tres permisos por defecto en el framework, por lo que no es necesario añadirlos manualmente, pero estos tres permisos se solicitan dinámicamente para permisos. En Android 12 y superior, deben solicitarse dinámicamente en el código.
```xml
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

### Inicialización
**Uso 1 (Recomendado):**
```java
QsBle.getInstance().init(Context context);
```
**Uso 2:**
```java
QsBle.getInstance().init(Context context,Handler handler);
```
*Nota 1: Todas las operaciones y devoluciones de llamada de QsBle a Ble se ejecutan en un solo hilo. La inicialización del modo 1 es usar el hilo implementado por el framework para operar Ble por defecto. Pero si deseas que todas las operaciones de Ble sean especificadas por ti mismo, también puedes pasar un Handler, de modo que todas las operaciones y devoluciones de llamada de Ble se llamarán en el Handler especificado <br/>Por ejemplo, si deseas que todas las operaciones se devuelvan en el hilo principal, puedes pasar el Handler del hilo principal, de modo que todas las operaciones y devoluciones de llamada de Ble se devuelvan en el hilo principal, **pero el autor no lo recomienda en absoluto***

*Nota 2: La inicialización de QsBle no obtendrá ninguna información privada del teléfono del usuario, así que puedes inicializar con tranquilidad en cualquier momento*


### Uso sencillo
**Uso 1:**
```java
QsBle.getInstance().startScan();
QsBle.getInstance().connect(mac);
QsBle.getInstance().connect(mac);
QsBle.getInstance().openNotify(mac,serviceuuid,notifyUuid);
QsBle.getInstance().writeByLock(mac,serviceUuid,writeUuid,value)
....
```

**Uso 2 (Cadena) (Recomendado para Java):**
```java
QsBle.getInstance().chain(mac)
    .connect()...
    .openNotify(serviceUuid,notifyUuid)...
    .requestConnectionToHigh()...
    .writeByLock(serviceUuid,writeUuid,value)...
    .writeByLock(serviceUuid,writeUuid,value)...
    .writeByLock(serviceUuid,writeUuid,value)...
    .disconnect()...
    ...
    .start()
```
*Nota: El proceso de operación en cadena anterior es: primero conectar al dispositivo cuya dirección es mac. Después de que la conexión sea exitosa, se abrirá la notificación del dispositivo. Después de que la notificación se abra correctamente, se establecerá el parámetro de conexión. Después de que el parámetro se establezca correctamente, se escribirán datos en la característica. Después de que el framework confirme que los datos se escribieron correctamente, se desconectará la conexión del dispositivo, y si alguna parte del enlace no logra el resultado esperado, no continuará ejecutando el resto*

**Uso 3 (Corrutinas de Kotlin) (Recomendado para Kotlin)**
```kotlin
//Usar corrutinas de kotlin para implementar la operación del modo 2
bleLifeScope.launch ({
    val chain = ble.chain(mac)
    //true: conexión exitosa
    val isConnect:Boolean=chain.connect().reConnectCount(3).connectTimeout(7000).dump(false).await()
    //Tres valores: notificación, indicación, desactivar, los dos primeros son notificaciones ya activadas
    val notifyStatus1:String?=chain.openNotify(serviceUuid,notifyUuid).dump(false).retry(3).await()
    val notifyStatus2:String?=chain.cancelNotify().await()
    //new int[]{interval, latency, timeout}
    //Siempre que no esté vacío, la configuración es exitosa
    val status1:IntArray?=chain.requestConnectionToLowPower().dump(false).await()
    val status2:IntArray?=chain.requestConnectionToBalanced().dump(false).await()
    val status3:IntArray?=chain.requestConnectionToHigh().dump(false).await()
    //true: escritura exitosa false: escritura fallida
    val status4:Boolean=chain.writeByLock(serviceUuid,writeUuid,value).dump(false).await()
    //true: escritura exitosa false: escritura fallida
    //La velocidad de norsp es aproximadamente 3-30 veces más rápida que sin norsp, lo cual está relacionado con los parámetros de conexión del dispositivo
    val status5:Boolean=chain.writeByLockNoRsp(serviceUuid,writeUuid,value).dump(false).await()
    //Devuelve el valor de la característica en el dispositivo
    val readValue:ByteArray?=chain.read(serviceUuid,readUuid).dump(false).await()
    //Devuelve el valor de la descripción en el dispositivo
    val readDescValue:ByteArray?=chain.readDesc(serviceUuid,readUuid,descUuid).dump(false).await()
    //new int[]{txPhy,rxPhy}
    val phy:IntArray?=chain.readPhy().await()
    //return rssi
    val rssi:Int?=chain.readRssi().await()
    //Devuelve el mtu que se ha establecido correctamente, si es null, la configuración falló.
    val mtu:Int?=chain.requestMtu(517).await()
    //Desconectar
    chain.disconnect().await()
},onError = {
    //Se llamará cuando haya un error en la ejecución de la corrutina, la devolución de llamada será en el hilo principal
},onStart = {
   //Se llama antes de que comience la ejecución de la corrutina, la devolución de llamada será en el hilo principal
},onFinally = {
    //Independientemente del éxito o fracaso de la ejecución de la corrutina, este método se llamará finalmente, y la devolución de llamada será en el hilo principal
})
```
*Nota: bleLifeScope es un objeto de extensión del objeto Lifecycle. Cuando el Lifecycle se destruye, bleLifeScope interrumpirá automáticamente la operación interna de la corrutina Bluetooth y destruirá la corrutina*
### Descripción de operadores públicos
**Las características de estos operadores son que todos admiten operaciones en cadena. Usar cada operador juntos puede resolver escenarios muy complejos.**

**Operador1:dump(boolean dump)**<br/>
**Explicación: Si el segmento de cadena actual termina toda la cadena de ejecución cuando falla la ejecución. El valor predeterminado es true, lo que termina toda la cadena**<br/>**Cadena:**
```java
//Conectar 5 dispositivos directamente por cadena
QsBle.getInstance().chain()
    .connect(mac1)...
    .connect(mac2).dump(false)...
    .connect(mac3).dump(true)...//El valor predeterminado es true, no se puede establecer
    .connect(mac4)
    .connect(mac5)
    .start()
```
**Explicación: Después de que la conexión de mac1 sea exitosa, se conectará a mac2. Si la conexión de mac2 falla, como un tiempo de espera de conexión o un error devuelto por el sistema, ¿mac3 continuará conectándose? La respuesta es sí, continuará conectándose. Porque el dump de la cadena de mac2 es false, incluso si la conexión falla, no interrumpirá toda la cadena. Mac3 continuará conectándose después de que la conexión de mac2 falle. Si la conexión de mac3 falla, ¿también fallará? Porque dump=true, la ejecución de toda la cadena se interrumpe directamente, y mac4 y mac5 no realizarán operaciones de conexión más tarde**<br/>**Corrutina:**
```kotlin
bleLifeScope.launch ({
    //Conectar 5 dispositivos directamente por cadena
    QsBle.getInstance().chain().apply{
        connect(mac1).await()
        connect(mac2).dump(false).await()
        connect(mac3).dump(true).await()//El valor predeterminado es true, no se puede establecer
        connect(mac4).await()
        connect(mac5).await()
    }
},onError = {
    //Se llamará cuando haya un error en la ejecución de la corrutina, la devolución de llamada será en el hilo principal
},onStart = {
   //Se llama antes de que comience la ejecución de la corrutina, la devolución de llamada será en el hilo principal
},onFinally = {
    //Independientemente del éxito o fracaso de la ejecución de la corrutina, este método se llamará finalmente, y la devolución de llamada será en el hilo principal
})
```
**Explicación: El proceso de ejecución en cadena es el mismo que el anterior. La única diferencia en el proceso de colaboración es que cuando el dump de esta cadena=true y ocurre un fallo de conexión, se lanzará una excepción directamente en el proceso de colaboración. Por lo tanto, cuando la conexión a mac3 falla, se llamará a la función onErro. Si deseas que falle sin lanzar una excepción, establece dump=false**<br/>

**Operador2:async())**<br/>
**Explicación: Quienes hayan usado la corrutina de kotlin deben conocer el papel de este operador. El papel de este operador es muy similar al de la corrutina. Se utiliza para ejecutar la cadena actual de forma asíncrona. Al ejecutar esta cadena, la siguiente cadena se devolverá exitosamente sin esperar el resultado de devolución de esta cadena**<br/>**Cadena:**
```java
//Conectar 5 dispositivos simultáneamente por cadena
QsBle.getInstance().chain()
    .connect(mac1).async()...
    .connect(mac2).async().dump(false)...
    //Dado que async devuelve éxito directamente y no hay fallo, el valor de dump no tiene efecto en la ejecución de toda la cadena
    .connect(mac3).async().dump(true)...//El valor predeterminado es true, no se puede establecer
    .connect(mac4).async()
    .connect(mac5).async()
    .start()
```
**Corrutina:**
```kotlin
bleLifeScope.launch ({
    //Debe tenerse en cuenta que incluso si llamas al operador async()
    //Pero luego llamas a await()
    //Entonces el operador async() se volverá ineficaz
    /**
     * Entonces el proceso de ejecución es esperar a que la conexión de mac1 tenga éxito
*Por supuesto, se lanzará una excepción directamente si la conexión a mac1 falla, porque dump es true por defecto
*Luego conectar los siguientes cuatro dispositivos simultáneamente
     * */
    QsBle.getInstance().chain().apply{
        connect(mac1).async().await()
        connect(mac2).async().start()
        connect(mac3).async().start()
        connect(mac4).async().start()
        connect(mac5).async().start()
    }
},onError = {
    //Se llamará cuando haya un error en la ejecución de la corrutina, la devolución de llamada será en el hilo principal
},onStart = {
   //Se llama antes de que comience la ejecución de la corrutina, la devolución de llamada será en el hilo principal
},onFinally = {
    //Independientemente del éxito o fracaso de la ejecución de la corrutina, este método se llamará finalmente, y la devolución de llamada será en el hilo principal
})
```
**Operador3:delay(long delay)**<br/>
**Explicación: El segmento de cadena actual retrasará la ejecución delay ms. Ten en cuenta que solo es el segmento de cadena actual**<br/>**Cadena:**
```java
QsBle.getInstance().chain()
    .connect(mac1)...
    .connect(mac2).delay(1000).dump(false)...
    .connect(mac3).delay(2000).dump(true)...//El valor predeterminado es true, no se puede establecer
    .connect(mac4).delay(3000)
    .connect(mac5).delay(4000)
    .start()
```
**Corrutina:**
```kotlin
bleLifeScope.launch ({
    QsBle.getInstance().chain().apply{
        connect(mac1).await()
        connect(mac2).await()
        //Esto usa la función de bloqueo proporcionada por la corrutina de kotlin
        delay(1000)
        //Esto usa el operador delay () implementado por el framework en sí
        //Aunque sus resultados de ejecución y tiempo son los mismos, debemos distinguirlos
        connect(mac3).delay(2000).await()
        connect(mac4).await()
        delay(3000)
        connect(mac5).delay(4000).await()
    }
},onError = {
    //Se llamará cuando haya un error en la ejecución de la corrutina, la devolución de llamada será en el hilo principal
},onStart = {
   //Se llama antes de que comience la ejecución de la corrutina, la devolución de llamada será en el hilo principal
},onFinally = {
    //Independientemente del éxito o fracaso de la ejecución de la corrutina, este método se llamará finalmente, y la devolución de llamada será en el hilo principal
})
```

**Operador4:retry(int retry)**<br/>
**Explicación: Número de re-ejecuciones después del fallo de ejecución del segmento de cadena actual**<br/>**Cadena:**
```java
QsBle.getInstance().chain()
    .connect(mac1)...
    //El operador retry funciona en la cadena actual
    //Si todas las conexiones de mac2 fallan, se reintentará la conexión 3 veces
    .connect(mac2).retry(3)...
    //Aquí, mac3 se reconectará 3 * 3=9 veces. El operador retry es un reintento del segmento de cadena actual
    .connect(mac3).reConnectCount(3).retry(3)...//El valor predeterminado es true, no se puede establecer
    .start()
```
**Corrutina:**
```kotlin
bleLifeScope.launch ({
    QsBle.getInstance().chain().apply{
        connect(mac1).await()
        connect(mac2).retry(3).await()
        connect(mac3).reConnectCount(3).retry(3).await()
    }
},onError = {
    //Se llamará cuando haya un error en la ejecución de la corrutina, la devolución de llamada será en el hilo principal
},onStart = {
   //Se llama antes de que comience la ejecución de la corrutina, la devolución de llamada será en el hilo principal
},onFinally = {
    //Independientemente del éxito o fracaso de la ejecución de la corrutina, este método se llamará finalmente, y la devolución de llamada será en el hilo principal
})
```
**Operador5:timeout(long timeout)**<br/>
**Explicación: El tiempo máximo de ejecución del segmento de cadena actual, es decir, el tiempo de espera agotado**<br/>**Cadena:**
```java
QsBle.getInstance().chain()
    /**
     *Si tarda 5000ms y timeout=4000ms en conectar con el dispositivo, la cadena se juzgará como fallida, pero el dispositivo está conectado
     *Esta cadena falló al ejecutarse
    **/
    .connect(mac1).connectTimeout(7000).timeout(4000)...
    .start()
```
**Corrutina:**
```kotlin
bleLifeScope.launch ({
    QsBle.getInstance().chain().apply{
        //¿Cuánto tiempo esperará el recorrido?
        //En el peor caso, espera 4000ms. Si se excede el tiempo de espera y no hay resultado, se juzgará que la ejecución falló
        connect(mac1).timeout(4000).retry(3).await()
    }
},onError = {
    //Se llamará cuando haya un error en la ejecución de la corrutina, la devolución de llamada será en el hilo principal
},onStart = {
   //Se llama antes de que comience la ejecución de la corrutina, la devolución de llamada será en el hilo principal
},onFinally = {
    //Independientemente del éxito o fracaso de la ejecución de la corrutina, este método se llamará finalmente, y la devolución de llamada será en el hilo principal
})
```
**Operador6:withMac(String mac)**<br/>
**Explicación: La dirección MAC del dispositivo correspondiente al segmento de cadena actual y la cadena ejecutada después de él. Este parámetro no es necesario excepto para la cadena escaneada. Otras cadenas son necesarias, pero algunas son implícitas y otras explícitas**<br/>

**Cadena:**
```java
//Toma el siguiente ejemplo inadecuado
QsBle.getInstance().chain()
            //Si se eliminan los siguientes comentarios, se reportará un error directamente cuando se ejecute la cadena aquí, porque no hay mac predeterminado entrante
            //.connect()
            .connect(mac1)
            .connect(mac2)
            //El valor0 se envía a mac2, porque el entrante aguas arriba es mac2
            .writeByLock(serviceUuid,chacUuid,value0).
            //Value1 se envía a mac1
            .writeByLock(serviceUuid,chacUuid,value1).withMac(mac1)
            //Value2 se envía a mac1
            .writeByLock(serviceUuid,chacUuid,value2)
            //Value3 se envía a mac1
            .writeByLock(serviceUuid,chacUuid,value3)
            //Value4 se envía a mac2 porque el valor mac cambia
            .writeByLock(serviceUuid,chacUuid,value4).withMac(mac2)
            //Value5 se envía a mac2
            .writeByLock(serviceUuid,chacUuid,value5)
            //Conectar mac3
            .connect().withMac(mac3)
            //Value6 se envía a mac3
            .writeByLock(serviceUuid,chacUuid,value6)
            //Value7 se envía a mac3
            .writeByLock(serviceUuid,chacUuid,value7)
            //Desconectar mac3, porque el entrante aguas arriba es mac3
            .disconnect()
            //Desconectar mac2
            .disconnect(mac2)
            //Desconectar mac3
            .disconnect(mac3)
            .start()
```
**Corrutina:**
```kotlin
Consulta el ejemplo proporcionado arriba
```
### Configuración global predeterminada
*Cambia la configuración global predeterminada y establece el valor correspondiente directamente*
```java
public class BleGlobalConfig {
    //Tiempo de escaneo predeterminado
    public static long scanTime = 20000;//ms
    //Número de reescrituras predeterminado del valor de característica de escritura
    public static int rewriteCount = 3;
    //Número de veces que falla el descubrimiento de servicios y se vuelve a descubrir por defecto
    public static int discoveryServiceFailRetryCount = 3;
    //UUID de servicio predeterminado
    public static UUID serviceUUID;
    //UUID de característica de escritura predeterminado
    public static UUID writeUUID;
    //UUID de notificación predeterminado
    public static UUID notifyUUID;
    //Tiempo de espera de conexión predeterminado
    public static int connectTimeout = 7000;
    //Número de reconexiones predeterminado
    public static int reconnectCount = 0;
    //Número predeterminado de veces que un paquete mtu único falla al reescribir su valor de escritura
    public static int singlePkgWriteTimeout = 200;
    //Número máximo de conexiones. Cuando el número de dispositivos conectados supera el número máximo de conexiones, el dispositivo más lejano se desconectará según el tiempo de conexión del dispositivo
    public static int maxConnectCount = 7;
    //Número predeterminado de veces que un paquete mtu único falla al reescribir su valor de escritura ota
    public static int otaSingleRewriteCount = 3;
    //Tamaño del segmento enviado por ota, como un tamaño de archivo de 1000b, otaSegmentSize=200b, se llamará cada 200 veces para el progreso
    public static int otaSegmentSize = 200;
    //Configuración de escaneo global
    public static SimpleScanConfig globalScanConfig;
    //El número predeterminado de reconexión automática tras la desconexión es 0, sin reconexión automática
    public static int autoReconnectCount = 0;

}
```
### Escuchar devoluciones de llamada Ble
Todas las funciones añadidas en forma de addXXXCallback deben ser eliminadas llamando al método rmXXXCallback correspondiente en el momento adecuado, o se producirán fugas de memoria
```java
    IConnectStatusChangeCallback connectStatusChangeCallback = (device, isConnect, status, profileState) -> {
        //Devolución de llamada del estado de conexión del dispositivo
    };
    IScanCallback scanCallback = (device, rssi, scanRecord) -> {
        //Devolución de llamada del dispositivo escaneado
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        QsBle.getInstance().addScanCallback(scanCallback);
        QsBle.getInstance().addConnectStatusChangeCallback(mac,connectStatusChangeCallback);
    }
```
Cuando la actividad se destruye, debes eliminar la devolución de llamada, o se producirán fugas de memoria o incluso reinicios de la aplicación
```java
    @Override
    protected void onDestroy() {
        QsBle.getInstance().rmScanCallback(scanCallback);
        QsBle.getInstance().rmConnectStatusChangeCallback(mac,connectStatusChangeCallback);
    }
```
QsBle también proporciona una mejor forma de agregar devoluciones de llamada vinculadas a un ciclo de vida
Por ejemplo, en activity, algunas actividades implementan la interfaz de la clase Lifecycle. Solo necesitas llamar al método addXXXCallback y pasar el parámetro Lifecycle. Cuando el objeto lifecycle se destruye, la devolución de llamada se eliminará automáticamente
```java
//Modo Kotlin
QsBle.getInstance().addConnectStatusChangeCallback(mac,this.lifecycle, IConnectStatusChangeCallback { device, isConnect, status, profileState -> 
        //Devolución de llamada del estado de conexión
 })
QsBle.getInstance().addScanCallback(this.lifecycle, IScanCallback { device, rssi, scanRecord -> 
        //Devolución de llamada del escaneo
})
//Modo java
QsBle.getInstance().addConnectStatusChangeCallback(mac,this.getLifecycle(), (device, isConnect, status, profileState) ->{
        //Devolución de llamada del estado de conexión
});
QsBle.getInstance().addScanCallback(this.getLifecycle(), (device, rssi, scanRecord) ->{
        //Devolución de llamada del escaneo
});
```

### Subcontratación y empaquetado más allá de Mtu
El mtu predeterminado de Android Ble es de 20 bytes de largo. Para datos mayores de 20 bytes, los desarrolladores de la aplicación y del dispositivo necesitan determinar el protocolo de subcontratación. Generalmente, el protocolo de subcontratación divide un dato mtu en encabezado del paquete y bits de datos. El encabezado del paquete especifica el número de serie de estos datos y otros datos. El dispositivo recibe un mtu para eliminar el encabezado del paquete y ensamblar los datos para formar un paquete completo

**1. Subcontratación**

Hay dos formas de escribir valores de características en QsBle: una es escribir datos de una longitud mtu, y la otra es escribir datos de más de una longitud mtu

**Método para escribir datos de longitud mtu:**
```java
void write(String mac,UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount);
void writeNoRsp(String mac,UUID serviceUuid, UUID chacUuid, byte[] value,int retryWriteCount);
```
**Un método para escribir datos más largos que un mtu:**
```java
void writeByLock(String mac,UUID serviceUuid, UUID chacUuid, byte[] value, int retryWriteCount, Function2<Boolean,Integer> writeCallbac);
void writeByLockNoRsp(String mac,UUID serviceUuid, UUID chacUuid, byte[] value,int retryWriteCount, Function2<Boolean,Integer> writeCallback);
```

**Las siguientes situaciones deben tenerse en cuenta al tratar con estas dos situaciones**
<br/>Nota: Es mejor usar una de estas dos formas en un tipo de dispositivo. * * El autor sugiere que todos los valores de características de escritura usen los dos métodos: writeByLock y writeByLockNoRsp * *. El método con Lock se refiere a que los datos escritos en las características del dispositivo entrarán en la cola de escritura, y los mensajes posteriores se enviarán después de recibir una devolución de llamada de escritura exitosa del sistema o activar un tiempo de espera. Dado que los métodos sin lock se envían directamente, no es necesario hacer cola. Si envías datos largos, y los datos largos no se envían todos a la vez, y luego llamas al método write para enviar un dato mtu, ocurrirá una colisión de paquetes en el lado del dispositivo, lo que provocará que los datos largos no sean reconocidos en el lado del dispositivo, a menos que tu encabezado del paquete contenga el bit de bandera de los datos largos o tus datos no necesiten ser subcontratados
Por lo tanto, el autor sugiere firmemente<br/>**Todos los valores de características de escritura usen los dos métodos: writeByLock y writeByLockNoRsp**

**2. Empaquetado**

QsBle también proporciona una interfaz para el empaquetado cuando el lado del dispositivo responde a mensajes que son más largos que un mtu
Solo necesitas implementar la clase abstracta BaseMultiPackageAssembly<br/>Aquí hay un ejemplo
```kotlin
class MyMulitPkgParser :
    BaseMultiPackageAssembly(){

    override fun onChanged(chac: BluetoothGattCharacteristic?, value: ByteArray?) {
        super.onChanged(chac, value)
    }

    override fun onError(e: Exception?) {
    }

    override fun isLastPkg(value: ByteArray): Boolean {
        //Determinar si los datos mtu devueltos son el último paquete para determinar si el ensamblaje fue exitoso
        //Si se devuelve true, se aceptan todos los datos
        //Si se devuelve false, indica que hay otro paquete y el ensamblaje no se puede determinar
    }

    override fun getPkgIndex(value: ByteArray): Int {
        //Obtener el número de serie del paquete de todos los datos de los datos mtu devueltos
    }
}
```
Después de implementar la clase de empaquetado, necesitas llamar a
```kotlin
QsBle.getInstance().setMultiPackageAssembly(mac, MyMulitPkgParser())
```
Establecer el analizador de paquetes global para este dispositivo
Después de que la configuración sea exitosa, solo necesitas agregar un oyente de devolución de llamada de paquetes cuando necesites obtener datos para obtener el paquete ensamblado exitosamente
```kotlin
ble.addBleMultiPkgsCallback(mac,MainActivity.nUuid, IBleMultiPkgsCallback { device, characteristic, result ->
    //Se devolverá todo después de que el empaquetado sea exitoso
    //El resultado es de tipo List<byte []>, que contiene todos los resultados después del ensamblaje
})
```
*Nota: Esto también requiere llamar a rmBleMultiPkgsCallback en el momento adecuado para eliminar la devolución de llamada*


### Actualización Ota
Para los desarrolladores de aplicaciones, Ota es cómo transferir un archivo al dispositivo. QsBle proporciona soporte completo para la actualización Ota
**Uso 1:**
Llama al método writeFile de QsBle:
```java
 QsBle.getInstance()
     .writeFileNoRsp(String mac, UUID serviceUuid, UUID chacUuid, byte[] fileBytes, IOtaUpdateCallback otaUpdateCallback);
 QsBle.getInstance()
     .writeFileNoRsp(String mac, UUID serviceUuid, UUID chacUuid, File file, IOtaUpdateCallback otaUpdateCallback)
//Todos los métodos anteriores encapsulan el método writeFileNoRsp de abajo. QsBle solo se enfoca en el flujo de E/S que envías
 QsBle.getInstance()
     .writeFileNoRsp(String mac, UUID serviceUuid,UUID chacUuid, int fileByteCount, int segmentLength,InputStream datasource,IOtaUpdateCallback otaUpdateCallback);
```
Descripción de parámetros:
fileByteCount:Longitud total en bytes del archivo ota a enviar
segmentLength:Longitud del segmento, cuánta longitud se llama para recordar el progreso del envío del archivo
datasource:Flujo de archivo ota entrante
otaUpdateCallback:Devolución de llamada de la actualización ota
```java
public interface IOtaUpdateCallback {
    //Se hará una devolución de llamada antes de enviar el archivo
    void onStart();
    //El archivo se ha enviado correctamente al dispositivo
    void onSuccess();
    //Si ocurre un error durante el envío del archivo, se hará una devolución de llamada
    void onError(Exception e);
    //Progreso del envío del archivo
    void onProgress(int progress);

}
```

**Uso 2 (Solo compatible con Kotlin):**
Enviar archivos ota utilizando la operación de proceso cooperativo proporcionada por el framework. En comparación con el Modo 1, este modo tiene un rendimiento peor, pero los desarrolladores pueden operar flexiblemente para hacer frente a diversos escenarios ota complejos
Idea de corrutina:<br/>Operación del modo 2 utilizando coproceso de kotlin<br/>**Código pseudocódigo**
```kotlin
bleLifeScope.launch ({
    val datasource:InputStream = ...
    val chain = ble.chain(mac)
    val value = ByteArray()
    while(datasource.available()>0){
        datasource.read(value)
        chain.writeByLock().await()
        //Escribe un fragmento de datos y luego procesa tu lógica. Por ejemplo, cada paquete ota incluye encabezado, pie de página y datos
    }
    
},onError = {
    //Se llamará cuando haya un error en la ejecución de la corrutina, la devolución de llamada será en el hilo principal
},onStart = {
   //Se llama antes de que comience la ejecución de la corrutina, la devolución de llamada será en el hilo principal
},onFinally = {
    //Independientemente del éxito o fracaso de la ejecución de la corrutina, este método se llamará finalmente, y la devolución de llamada será en el hilo principal
})
```

**Uso 3:**
Usar operación en cadena para enviar archivos recursivamente<br/>Idea de recursividad en cadena:<br/>**Código pseudocódigo***
```java
public void writeFile(byte[] value){
   QsBle.getInstance().chain(mac).writeByLockNoRsp(value).start((isSuccess, e) -> {
            if (isSuccess){
                //Rellenar arreglo de bytes
                writeFile(value)
            }
    });
}
```
### TODO
- [ ] Documentación
- [ ] Tutorial de Android Ble
- [ ] Acceso a Bluetooth clásico
- [ ] Soporte de función periférica Ble
- [ ] Próximamente
