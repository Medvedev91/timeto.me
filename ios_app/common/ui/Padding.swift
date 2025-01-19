import SwiftUI

struct Padding: View {
    
    var horizontal: Double = 0
    var vertical: Double = 0
    
    var body: some View {
        ZStack {
        }
        .frame(width: horizontal)
        .frame(height: vertical)
    }
}
