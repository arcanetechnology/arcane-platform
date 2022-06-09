package no.arcane.platform.app.trade.utils.virtualaccount

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val exchange = listOf(
    "Coinbase",
    "FTX",
    "Binance",
    "Kraken",
)

private val broker = listOf(
    "B2C2",
    "Interactive Brokers",
    "Enigma",
    "Sparebank 1 Markets",
)

private val fiat = listOf(
    "USD",
    "EUR",
    "GBP",
    "NOK",
    "SEK",
    "DKK",
    "CHF",
)

private val bank = listOf(
    "SP1",
    "Clearjunction",
    "SBanken",
)

private val vault = listOf(
    "Fireblocks",
    "Metamask",
)

data class Network(
    val id: String,
    val gas: String,
)

private val networkList = listOf(
    Network("ERC20","ETH"),
    Network("POLYGON","MATIC"),
    Network("SOL","SOL"),
    Network("BSC","BNB"),
    Network("TRX","TRX"),
    Network("BNB","BNB"),
    Network("AVAX","AVAX"),
    Network("AVAXC","AVAXC"),
    Network("FANTOM","FANTOM"),
)

private val network = networkList.map(Network::id)

private val gasToken = networkList.map { "${it.id}_${it.gas}" }

private val coin = listOf(
    "ALGO",
    "REP",
    "BAT",
    "XBT",
    "BCH",
    "ADA",
    "LINK",
    "ATOM",
    "DAI",
    "XDG",
    "EOS",
    "ETH",
    "ETC",
    "GNO",
    "LTC",
    "NANO",
    "OMG",
    "OXT",
    "PAXG",
    "XRP",
    "XLM",
    "XTZ",
    "TRX",
    "DOT",
    "SOL",
    "MATIC",
    "USDT",
    "USDC",
    "AAVE",
    "UNI",
    "CRV",
    "COMP",
    "GRT",
    "SNX",
    "YFI",
    "MANA",
    "NMR",
    "SAND",
    "BUSD",
    "BNB",
    "FTT",
    "FTM",
    "AVAX",
    "BTC",
)

val crypto: List<String> = with(AdtContext('_')) {
    coin + (coin * network) + gasToken
}

private val virtualAccountTypes = with(AdtContext(' ')) {
    listOf(
        VirtualAccountType("User", fiat + crypto),
        VirtualAccountType("Arcane", fiat + crypto),
        VirtualAccountType(bank * "Fee", fiat),
        VirtualAccountType(bank * "Tax", fiat, allowNegative = false),
        VirtualAccountType("Blockchain Fee", crypto),
        VirtualAccountType(exchange * "Withdrawal Fee", crypto, allowNegative = false),
        VirtualAccountType(exchange * "Fee", fiat),
        VirtualAccountType(broker * "Withdrawal Fee", crypto, allowNegative = false),
        VirtualAccountType(broker * "Fee", fiat),
        VirtualAccountType(vault * "Fee", fiat),
    )
}

fun main() {
    println(Json.encodeToString(virtualAccountTypes.toVirtualAccountOptionList()))
//    println("id,label,currency,allowNegative")
//    virtualAccountTypes
//        .toVirtualAccountOptionList()
//        .forEach {
//            with(it) {
//                println("$id,$label,$currency,$allowNegative")
//            }
//        }
}