import SwiftUI
import shared

struct PrivacySheet: View {

    @Binding var isPresented: Bool

    @State private var vm = PrivacySheetVM()

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            Text("todo")
        }
    }
}
