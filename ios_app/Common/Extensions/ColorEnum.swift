import SwiftUI
import shared

extension ColorEnum {
    
    func toColor() -> Color {
        switch self {
        case .white: .white
        case .black: .black
            
        case .red: .red
        case .orange: .orange
        case .yellow: .yellow
        case .green: .green
        case .mint: .mint
        case .teal: .teal
        case .cyan: .cyan
        case .blue: .blue
        case .indigo: .indigo
        case .purple: .purple
        case .pink: .pink
        case .brown: .brown
            
        case .gray: .gray
            
        default: fatalError("ColorEnum.toColor() no \(self)")
        }
    }
}
