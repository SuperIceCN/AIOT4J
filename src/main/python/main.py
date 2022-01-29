import time
import usocket
import ssd1306
import machine

# 延迟间隔
DELAY_TIME = 100
# AIOT4J jvm地址
AIOT4J_ADDRESS = "192.168.8.136"
# AIOT4J端口
AIOT4J_PORT = 14213

global sock # type: socket
global screen # type: Screen
global rocker  # type: Rocker

class Screen:
    def __init__(self, scl=16, sda=17) -> None:
        i2c = machine.I2C(scl=machine.Pin(
            scl), sda=machine.Pin(sda), freq=400000)
        self.oled = ssd1306.SSD1306_I2C(128, 64, i2c)

    def text(self, str  # type: str
             ):
        self.oled.show_fill(0)
        lines = str.split("\n")
        for i in range(0, len(lines)):
            self.oled.text(lines[i], 0, 12 * i)
        self.oled.show()
        return self

    KEY = [" ", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
           "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"]

    def board(self, index):
        if index < 0:
            index = 0
        elif index >= len(self.KEY):
            index = len(self.KEY) - 1
        self.oled.show_fill_rect(0, 46, 128, 8, 0)
        self.oled.text(" ".join(self.KEY[index:index + 8]), 0, 46)
        self.oled.show()
        return self.KEY[index]


class Rocker:
    def __init__(self, x_adc, y_adc, z_adc) -> None:
        self.xadc = machine.ADC(machine.Pin(x_adc))
        self.xadc.atten(machine.ADC.ATTN_11DB)
        self.yadc = machine.ADC(machine.Pin(y_adc))
        self.yadc.atten(machine.ADC.ATTN_11DB)
        self.zadc = machine.ADC(machine.Pin(z_adc))
        self.zadc.atten(machine.ADC.ATTN_11DB)
        pass

    def is_left(self):
        return self.xadc.read() < 255

    def is_right(self):
        return self.xadc.read() > 4000

    def is_up(self):
        return self.yadc.read() < 255

    def is_down(self):
        return self.yadc.read() > 4000

    def is_push(self):
        return self.zadc.read() == 0

class Screen:
    def __init__(self, scl=16, sda=17) -> None:
        i2c = machine.I2C(scl=machine.Pin(
            scl), sda=machine.Pin(sda), freq=400000)
        self.oled = ssd1306.SSD1306_I2C(128, 64, i2c)

    def text(self, str  # type: str
             ):
        self.oled.show_fill(0)
        lines = str.split("\n")
        for i in range(0, len(lines)):
            self.oled.text(lines[i], 0, 12 * i)
        self.oled.show()
        return self

    KEY = [" ", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o",
           "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"]

    def board(self, index):
        if index < 0:
            index = 0
        elif index >= len(self.KEY):
            index = len(self.KEY) - 1
        self.oled.show_fill_rect(0, 46, 128, 8, 0)
        self.oled.text(" ".join(self.KEY[index:index + 8]), 0, 46)
        self.oled.show()
        return self.KEY[index]

def main():
    print("Running")
    # 初始化操作杆
    init_rocker()
    # 初始化显示屏
    init_oled()
    # 连接到wifi
    connect_wifi()
    # 连接到AIOT4J
    aiot4j_connect()
    # 启动AIOT4J事件循环
    aiot4j_eventloop()

def init_oled():
    global screen
    screen = Screen()


def init_rocker():
    global rocker
    rocker = Rocker(35, 36, 39)


def connect_wifi():
    global rocker
    import network
    wlan = network.WLAN(network.STA_IF)
    if not wlan.active():
        wlan.active(True)
    if wlan.isconnected():
        print("already connected")
        screen.text("     AIOT4J")
        return
    scan_result = wlan.scan()
    wifi_ssid_list = []
    for each in scan_result:
        tmp = each[0].decode()
        if all(ord(i) < 128 for i in tmp):
            wifi_ssid_list.append(tmp)
    screen.text("> " + "\n  ".join(wifi_ssid_list))
    index = 0
    wifi_ssid = ""
    print("1")
    while True:
        if rocker.is_down():
            index = (
                index + 1) if index < len(wifi_ssid_list) else len(wifi_ssid_list)
            screen.text("> " + "\n  ".join(wifi_ssid_list[index:]))
        elif rocker.is_up():
            index = (index - 1) if index != 0 else 0
            screen.text("> " + "\n  ".join(wifi_ssid_list[index:]))
        elif rocker.is_push():
            wifi_ssid = wifi_ssid_list[index]
            screen.text(wifi_ssid+"\n"+"Password:\n\n\n\n^")
            break
        time.sleep(0.2)

    index = 1
    key = ""
    password = ""
    time.sleep(0.2)
    while True:
        if rocker.is_left():
            index += 1
            key = screen.board(index)
        elif rocker.is_right():
            index -= 1
            key = screen.board(index)
        elif rocker.is_down():
            password += key
            screen.text(wifi_ssid+"\n"+"Password:\n"+password+"\n\n\n^")
            key = screen.board(index)
        elif rocker.is_up():
            password = password[:-1]
            screen.text(wifi_ssid+"\n"+"Password:\n"+password+"\n\n\n^")
            key = screen.board(index)
        elif rocker.is_push():
            print("1999999999999999999999999999\n999999999")
            break
        time.sleep(0.2)
    wlan.connect(wifi_ssid, password)
    while not wlan.isconnected():
        time.sleep(0.2)
    print("successfully connected to " + wifi_ssid)
    screen.text("     AIOT4J")

def aiot4j_connect():
    sockaddr = usocket.getaddrinfo(AIOT4J_ADDRESS, AIOT4J_PORT)[0][-1]
    global sock
    sock = usocket.socket(usocket.AF_INET, usocket.SOCK_STREAM)
    sock.connect(sockaddr)
    print("AIOT4J has successfully connected!")

def aiot4j_eventloop():
    global sock
    sock.send(b"connecting\n")
    while True:
        buffer = sock.readline()
        if len(buffer) != 0:
            buffer = buffer.decode()
            buffer = buffer[0:len(buffer)-1]
            if buffer == "closeClient":
                sock.send("closeServer\n")
                sock.close()
                break
            else:
                try:
                    sock.send(str(eval(buffer)) + "\n")
                except:
                    sock.send("Error encountered!\n")

if __name__ == '__main__':
    main()