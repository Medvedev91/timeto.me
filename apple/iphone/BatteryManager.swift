import UIKit
import shared

class BatteryManager {
    
    init() {
        UIDevice.current.isBatteryMonitoringEnabled = true
        BatteryManager.upBatteryState()
        BatteryManager.upBatteryLevel()
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(batteryStateDidChange(notification:)),
            name: UIDevice.batteryStateDidChangeNotification,
            object: nil
        )
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(batteryLevelDidChange(notification:)),
            name: UIDevice.batteryLevelDidChangeNotification,
            object: nil
        )
    }
    
    @objc private func batteryStateDidChange(notification: Notification) {
        BatteryManager.upBatteryState()
    }
    
    @objc private func batteryLevelDidChange(notification: Notification) {
        BatteryManager.upBatteryLevel()
    }
    
    private static func upBatteryLevel() {
        let rawLevel: Int = Int(UIDevice.current.batteryLevel * 100)
        let level: Int = rawLevel < 0 ? 100 : rawLevel
        BatteryInfo.shared.emitLevel(level: level.toInt32())
    }
    
    private static func upBatteryState() {
        let state = UIDevice.current.batteryState
        let isCharging: Bool
        switch state {
        case .unknown:
            isCharging = false
        case .unplugged:
            isCharging = false
        case .charging:
            isCharging = true
        case .full:
            isCharging = true
        @unknown default:
            isCharging = false
        }
        BatteryInfo.shared.emitIsCharging(isCharging: isCharging)
    }
}
