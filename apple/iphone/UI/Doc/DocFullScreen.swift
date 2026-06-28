import SwiftUI
import shared

struct DocFullScreen: View {
    
    let forceRead: Bool
    
    var body: some View {
        VmView({
            DocVm()
        }) { vm, state in
            let state = vm.state.value as! DocVm.State
            DocFullScreenInner(
                vm: vm,
                state: state,
                forceRead: forceRead,
            )
        }
    }
}

private struct DocFullScreenInner: View {
    
    let vm: DocVm
    let state: DocVm.State
    
    let forceRead: Bool
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        
        List {
            
            if forceRead {
                
                PView {
                    Text("I force you to read this guide because without it, you will not understand how to use the app.")
                        .forceText()
                        .padding(.top, 4)
                }
                
                PView {
                    Text("Please DO NOT SKIP this! It will help you get started and begin improving your life.")
                        .forceText()
                }
                
                PView {
                    Text("Good luck!")
                        .forceText()
                }
                
                Divider()
                    .fillMaxWidth()
                    .frame(height: 1)
                    .background(.separator)
            }
            
            PView {
                Text("I built this app to manage my productivity. Here, I will ") +
                Text("SHARE")
                    .greenSemiBold() +
                Text(" my productivity system and how I use the app.")
            }
            
            PView {
                Text("My system ") +
                Text("IS NOT")
                    .redSemiBold() +
                Text(" about time tracking, ") +
                Text("IS NOT")
                    .redSemiBold() +
                Text(" about getting nice activity charts, ") +
                Text("IS NOT")
                    .redSemiBold() +
                Text(" about reducing wasted time.")
            }
        }
        .listStyle(.plain)
        .navigationTitle("How to Use the App")
        .toolbar {
            if !forceRead {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") {
                        dismiss()
                    }
                }
            }
        }
    }
}

private struct PView<Content: View>: View {
    
    @ViewBuilder private let content: () -> Content
    
    init(
        _ content: @escaping () -> Content,
    ) {
        self.content = content
    }
    
    var body: some View {
        ZStack {
            content()
        }
        .listRowSeparator(.hidden)
    }
}

private extension Text {
    
    func redSemiBold() -> Text {
        foregroundColor(.red).fontWeight(.semibold)
    }
    
    func greenSemiBold() -> Text {
        foregroundColor(.green).fontWeight(.semibold)
    }
    
    func blueSemiBold() -> Text {
        foregroundColor(.blue).fontWeight(.semibold)
    }
    
    ///
    
    func forceText() -> Text {
        foregroundColor(.blue).font(.system(size: 20, weight: .bold))
    }
}
