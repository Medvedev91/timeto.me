import SwiftUI
import CoreMotion

@Observable
class OrientationManager {
    
    static let instance = OrientationManager()
    
    var orientationMask: UIInterfaceOrientationMask = .portrait
    
    private let motionManager = CMMotionManager()
    
    func start() {
        stop()
        
        guard motionManager.isDeviceMotionAvailable else { return }
        
        // Update interval 4 times a second
        motionManager.deviceMotionUpdateInterval = 0.25
        
        motionManager.startDeviceMotionUpdates(to: .main) {[weak self] motion, _ in
            guard let self = self, let motion = motion else { return }
            
            let gravityX: Double = motion.gravity.x
            let gravityY: Double = motion.gravity.y
            
            if abs(gravityY) > 0.5 {
                self.updateOrientation(.portrait)
            } else if gravityX < -0.70 {
                self.updateOrientation(.landscapeRight)
            } else if gravityX > 0.70 {
                self.updateOrientation(.landscapeLeft)
            }
        }
    }
    
    func stop() {
        motionManager.stopDeviceMotionUpdates()
    }
    
    private func updateOrientation(_ orientation: UIInterfaceOrientationMask) {
        if UIDevice.current.userInterfaceIdiom != .phone {
            return
        }
        guard let windowScene = (UIApplication.shared.connectedScenes.first as? UIWindowScene) else {
            return
        }
        if orientationMask == orientation {
            return
        }
        orientationMask = orientation
        windowScene.requestGeometryUpdate(.iOS(interfaceOrientations: orientation))
        windowScene.keyWindow?.rootViewController?.setNeedsUpdateOfSupportedInterfaceOrientations()
    }
}
