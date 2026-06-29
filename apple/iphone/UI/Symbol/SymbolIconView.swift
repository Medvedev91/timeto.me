import SwiftUI
import shared

struct SymbolIconView: View {
    
    let icon: Symbol.Icon
    let color: Color
    let size: CGFloat
    
    var body: some View {
        Image(systemName: icon.systemName())
            .font(.system(size: size, weight: .semibold))
            .foregroundColor(color)
    }
}

private extension Symbol.Icon {
    
    func systemName() -> String {
        switch self.iconEnum {
        case .book: "book.closed.fill"
        case .case_: "case.fill"
        case .timer: "timer"
        case .exercise: "dumbbell.fill"
        case .piano: "pianokeys.inverse"
        case .musicNote: "music.note"
        case .option: "option"
        case .graduationcap: "graduationcap.fill"
        case .megaphone: "megaphone.fill"
        case .instruments: "wrench.and.screwdriver.fill"
        case .meditation: "figure.mind.and.body"
        case .rocket: "airplane.up.forward" // todo no rocket symbol :(
        case .flask: "flask.fill"
        case .compass: "compass.drawing"
        case .gamecontroller: "gamecontroller.fill"
        case .soccerball: "soccerball.inverse"
        case .hiking: "figure.hiking"
        case .bus: "bus"
        case .bulb: "lightbulb.fill"
        case .bolt: "bolt.fill"
        case .inbox: "tray.fill"
        case .sun: "sun.min.fill"
        case .moon: "moon.fill"
        case .moonStars: "moon.stars.fill"
        case .question: "questionmark"
        default: "questionmark"
        }
    }
}
